package com.vub.pdproject;

import com.vub.pdproject.data.YelpData;
import com.vub.pdproject.data.models.Business;
import com.vub.pdproject.data.models.Review;
import com.vub.pdproject.data.readers.YelpBusinessReader;
import com.vub.pdproject.data.readers.YelpReviewReader;
import com.vub.pdproject.search.ParallelSearch;
import com.vub.pdproject.search.QueryEngine;

import java.io.IOException;
import java.lang.System;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;


class ReviewProxy{
    Boolean available;
    private Review review;
    String reviewID;
    private RMIYelpServerProtocol vault;


    public ReviewProxy(Review review, String reviewID){
        this.review = review;
        this.reviewID = reviewID;
        this.available = true;
    }

    public ReviewProxy(RMIYelpServerProtocol vault, String reviewID){
        this.vault = vault;
        this.reviewID = reviewID;
        this.available = false;
    }

    public Review getReview() throws RemoteException {
        if (available){
            return review;
        } else {
            try {
                vault.ping(); // Check to make sure the vault is still alive.
                return vault.getReviews(Collections.singletonList(reviewID)).get(0);
            } catch (RemoteException e) {
                return null;
            }
        }
    }
}


public class RMIDataVault implements RMIYelpServerProtocol {
    //YelpData dataset;
    QueryEngine qe;
    RMIYelpServerProtocol server;
    //private final Map<String, Review> reviews;
    //private final Map<String, Business> businesses;

    //YelpData dataset;

    Map<String, ReviewProxy> reviews;
    Map<String, Business> businesses;
    Map<RMIYelpServerProtocol, Integer> otherVaults;


    int MAX_BUSINESSES;
    int MAX_REVIEWS;
    int nodeN;

    RMIDataVault(int nodeNumber, int totalNondes, int maxBusinesses, int maxReviews, String business_loc, String review_loc) throws IOException {
        qe = new ParallelSearch(4, 0);
        MAX_BUSINESSES = maxBusinesses;
        MAX_REVIEWS = maxReviews;
        nodeN = nodeNumber;
        otherVaults = new HashMap<>();
        reviews = new HashMap<String, ReviewProxy>();
        Map<String, Review> reviewsFromJson = YelpReviewReader.readData(review_loc);
        for (Review rev : reviewsFromJson.values()){
            reviews.put(rev.id, new ReviewProxy(rev, rev.id));
        }

        businesses = new HashMap<String, Business>();
        businesses = YelpBusinessReader.readData(business_loc);
        System.out.println("VAULT: Vault("+nodeN+"/"+totalNondes+") contains "+reviews.size()+" reviews and "+businesses.size()+" businesses");
    }

    private void verifyVaultConnections() {
        List<RMIYelpServerProtocol> brokenVaults = new ArrayList<>();
        for (RMIYelpServerProtocol vault : otherVaults.keySet()){
            try {
                vault.ping();
            } catch (RemoteException e) {
                System.out.println("SERVER: Vault("+otherVaults.get(vault)+") has disconnected. Removing it.");
                brokenVaults.add(vault);
            }
        }
        for (RMIYelpServerProtocol v : brokenVaults)
            otherVaults.remove(v);
    }

    @Override
    public ArrayList<Review> getReviews(List<String> reviewIDs) throws RemoteException {
        ArrayList<Review> results = new ArrayList<Review>();
        for(String reviewID : reviewIDs){
            ReviewProxy reviewProxy = reviews.get(reviewID);
            if (reviewProxy != null) {
                results.add(reviewProxy.getReview());
            }
        }
        return results;
    }

    // Only returns reviews from businesses stored on this vault. Not reviews from other vaults stored here.
    @Override
    public Map<String, Review> getAllReviews() throws RemoteException {
        Map<String, Review> result = new HashMap<>();
        for (ReviewProxy rev : reviews.values()){
            if (rev.available)
                result.put(rev.reviewID, rev.getReview());
        }
        return result;
    }

    @Override
    public Map<String, Business> getAllBusinesses() throws RemoteException {
        return businesses;
    }

    @Override
    public Review addReview(String businessID, String reviewText, int reviewStars) throws RemoteException {
        if (!businesses.containsKey(businessID))
            return null; // This vault does not contain this business.

        Review review = new Review();
        review.text = reviewText;
        review.stars = reviewStars;
        review.businessId = businessID;
        review.id = generateUniqueID();
        Business business = businesses.get(businessID);
        ReviewProxy reviewProxy = null;
        if (getAllReviews().size() >= MAX_REVIEWS) {
            System.out.println("VAULT: Review could not be added because vault(" + nodeN + ") is full. Trying to store it elsewhere.");
            verifyVaultConnections();
            for (Map.Entry<RMIYelpServerProtocol, Integer> otherVaultsEntry : otherVaults.entrySet()) {
                Review remoteReview = otherVaultsEntry.getKey().addRemoteReview(review);
                if (remoteReview != null) {
                    reviewProxy = new ReviewProxy(otherVaultsEntry.getKey(), review.id);
                    System.out.println("VAULT: Successfully added remote review on vault(" + otherVaultsEntry.getValue() + ").");
                    break;
                }
            }
            if (reviewProxy == null)
                System.out.println("VAULT: Review could not be added anywhere.");

        } else {
            reviewProxy = new ReviewProxy(review, review.id);
        }

        if (reviewProxy == null)
            return null;

        reviews.put(review.id, reviewProxy);
        if (business.reviews.size() == 0)
            business.stars = review.stars;
        else
            business.stars = business.stars + ((review.stars - business.stars) / business.reviews.size());
        business.reviews.add(review.id);
        businesses.put(businessID, business);
        System.out.println("VAULT: Added review to vault("+nodeN+"): " + review);
        return review;

    }

    @Override
    public Review addRemoteReview(Review review) throws RemoteException {
        if (getAllReviews().size() < MAX_REVIEWS){
            reviews.put(review.id, new ReviewProxy(review, review.id));
            System.out.println("VAULT: Added remote review to vault("+nodeN+"): " + review);
            return review;
        }
        return null;
    }

    @Override
    public Business addBusiness(String name, String address, String city) throws RemoteException {
        if (getAllBusinesses().size() >= MAX_BUSINESSES){
            System.out.println("VAULT: Business could not be added because vault("+nodeN+") is full.");
            return null;
        }
        String business_id = generateUniqueID();
        Business business = new Business();
        business.name = name;
        business.address = address;
        business.city = city;
        business.id = business_id;
        business.stars = 0;
        businesses.put(business_id, business);
        System.out.println("VAULT: Added business  to vault("+nodeN+"): " + business);
        return business;
    }

    @Override
    public void connectDataVault(RMIYelpServerProtocol vault, Integer nodeN) throws RemoteException {
        if (nodeN == this.nodeN){
            // Vault needs to be sent to server.
            server.connectDataVault(vault, nodeN);
        } else {
            // Vault was received from server.
            System.out.println("VAULT: vault("+this.nodeN+"): received vault("+nodeN+"): ");
            otherVaults.put(vault, nodeN);
        }
    }

    // Lazy way to create a pseudo-unique id. Assumes no two reviews will be created at the same time.
    private String generateUniqueID(){
        Date date = new Date();
        long timeMilli = date.getTime();
        return Long.toString(timeMilli);
    }

    @Override
    public Map<QueryEngine.RRecord, Business> search(String search_string) throws RemoteException {
        HashMap<String, Review> reviewsObjects = new HashMap<>();
        for (ReviewProxy revp : reviews.values()){
            reviewsObjects.put(revp.reviewID, revp.getReview());
        }
        YelpData dataset = new YelpData(businesses, reviewsObjects);
        List<QueryEngine.RRecord> rbids = qe.search(search_string, dataset);
        Map<QueryEngine.RRecord, Business> results = new TreeMap<>(); // Automaticaaly sorth the results.

        for(QueryEngine.RRecord rbid : rbids){
            Business bd = dataset.getBusiness(rbid.businessID);
            results.put(rbid, bd);
        }
        return results;
    }
    @Override
    public Business getBusiness(String businessID) throws RemoteException {
        return businesses.get(businessID);
    }

    @Override
    public void ping() {
    }

    void initialize(int regPort) {
        try {
//            System.setProperty("java.security.policy","file:src/security.policy");
//            if (System.getSecurityManager() == null) {
//                System.setSecurityManager(new SecurityManager());
//            }

            Registry reg = LocateRegistry.getRegistry("localhost", regPort);
            server = (RMIYelpServerProtocol) reg.lookup("RMIYelpServer");
            RMIYelpServerProtocol stub = (RMIYelpServerProtocol) UnicastRemoteObject.exportObject(this, 0);
            connectDataVault(stub, nodeN);

        } catch(RemoteException e) {
            System.out.println("Client remote exception: " + e.getMessage());
        } catch(NotBoundException e) {
            System.out.println("Client not bound exception: " + e.getMessage());
        }
    }


    public static void main(String[] args) throws IOException {
        int NODE;
        int TOTAL_NODES;
        int MAX_BUSINESSES;
        int MAX_REVIEWS;
        int regPort; // = 1234;
        String review_loc;
        String businesses_loc;


        if (args.length == 3 && args[0].equals("demo")){
            System.out.println("Started vault with demo configuration. ");
            NODE = Integer.parseInt(args[1]);
            regPort = Integer.parseInt(args[2]);
            TOTAL_NODES = 3;
            MAX_BUSINESSES = 3;
            MAX_REVIEWS = 3;
            review_loc = "data/presets/demonstration/reviewsVault"+NODE+".json";
            businesses_loc = "data/presets/demonstration/businessesVault"+NODE+".json";
            RMIDataVault vault = new RMIDataVault(NODE, TOTAL_NODES, MAX_BUSINESSES, MAX_REVIEWS, businesses_loc, review_loc);
            vault.initialize(regPort);

        } else if (args.length == 5){
            System.out.println("Started vault with a preset configuration. ");
            NODE = Integer.parseInt(args[0]);
            TOTAL_NODES = Integer.parseInt(args[1]);
            MAX_BUSINESSES = Integer.parseInt(args[2]);
            MAX_REVIEWS = Integer.parseInt(args[3]);
            review_loc = "data/presets/" + NODE + "/reviews.json";
            businesses_loc = "data/presets/" + NODE + "/reviews.json";
            regPort = Integer.parseInt(args[4]);
            RMIDataVault vault = new RMIDataVault(NODE, TOTAL_NODES, MAX_BUSINESSES, MAX_REVIEWS, businesses_loc, review_loc);
            vault.initialize(regPort);

        } else {
            System.out.println("Args are not correct.");
        }
    }
}
