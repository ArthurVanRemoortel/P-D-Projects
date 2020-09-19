package com.vub.pdproject;


import com.vub.pdproject.data.models.Business;
import com.vub.pdproject.data.models.Review;
import com.vub.pdproject.search.ParallelSearch;
import com.vub.pdproject.search.QueryEngine;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;


public class RMIYelpServer implements RMIYelpServerProtocol {
    Map<RMIYelpServerProtocol, Integer> vaults;


    public RMIYelpServer() {
        vaults = new HashMap<>();
    }


    private void verifyVaultConnections() {
        List<RMIYelpServerProtocol> brokenVaults = new ArrayList<>();
        for (RMIYelpServerProtocol vault : vaults.keySet()){
            try {
                vault.ping();
            } catch (RemoteException e) {
                System.out.println("SERVER: Vault("+vaults.get(vault)+") has disconnected. Removing it.");
                brokenVaults.add(vault);
            }
        }
        for (RMIYelpServerProtocol v : brokenVaults)
            vaults.remove(v);
    }

    @Override
    public Map<QueryEngine.RRecord, Business> search(String search_string) throws RemoteException {
        verifyVaultConnections();
        TreeMap<QueryEngine.RRecord, Business> sortedResults = new TreeMap<>();
        for (RMIYelpServerProtocol vault : vaults.keySet()){
            sortedResults.putAll(vault.search(search_string));
        }
        //for(Map.Entry<QueryEngine.RRecord, Business> bdEntry : sortedResults.entrySet()){
        //    System.out.println(bdEntry.getValue().name+" ("+bdEntry.getKey().relevance_score+")");
        //}
        return sortedResults;
    }

    @Override
    public Business addBusiness(String name, String address, String city) throws RemoteException {
        verifyVaultConnections();
        for (RMIYelpServerProtocol vault : vaults.keySet()){
            Business business = vault.addBusiness(name, address, city);
            if (business != null){
                return business;
            } else {
                System.out.println("SERVER: Business could not be added to vault("+vaults.get(vault)+"). Trying another one...");
            }
        }
        // Business could not be added anywhere.
        System.out.println("SERVER: Business could not be added anywhere.");
        return null;
    }

    @Override
    public Business getBusiness(String businessID) throws RemoteException {
        verifyVaultConnections();
        for (RMIYelpServerProtocol vault : vaults.keySet()){
            Business business = vault.getBusiness(businessID);
            if (business != null)
                return business;
        }
        return null;
    }

    @Override
    public Review addReview(String businessID, String reviewText, int reviewStars) throws RemoteException {

        RMIYelpServerProtocol targetVault = null;
        for (RMIYelpServerProtocol vault : vaults.keySet()){
            if (vault.getBusiness(businessID) != null){
                targetVault = vault;
                break;
            }
        }
        Review review = targetVault.addReview(businessID, reviewText, reviewStars);
        if (review != null){
            return review;
        } else {
            System.out.println("SERVER: Review could not be added anywhere.");
            return null;
        }
    }

    @Override
    public Review addRemoteReview(Review review) throws RemoteException {
        return null;
    }

    @Override
    public ArrayList<Review> getReviews(List<String> reviewIDs) throws RemoteException {
        verifyVaultConnections();
        ArrayList<Review> reviews = new ArrayList<Review>();
        for (RMIYelpServerProtocol vault : vaults.keySet()){
            List<Review> vaultReviews = vault.getReviews(reviewIDs);
            reviews.addAll(vaultReviews);
        }
        return reviews;
    }

    @Override
    public Map<String, Review> getAllReviews() throws RemoteException {
        verifyVaultConnections();
        Map<String, Review> reviews = new HashMap<>();
        for (RMIYelpServerProtocol vault : vaults.keySet()){
            Map<String, Review> vaultReviews = vault.getAllReviews();
            reviews.putAll(vaultReviews);
        }
        return reviews;
    }

    @Override
    public Map<String, Business> getAllBusinesses() throws RemoteException {
        verifyVaultConnections();
        Map<String, Business> businesses = new HashMap<>();
        for (RMIYelpServerProtocol vault : vaults.keySet()){
            Map<String, Business> vaultBusinesses = vault.getAllBusinesses();
            businesses.putAll(vaultBusinesses);
        }
        return businesses;
    }

    /**
     * @param vault newly booted vault.
     * @param nodeN unique node number of the vault.
     */
    @Override
    public void connectDataVault(RMIYelpServerProtocol vault, Integer nodeN) throws RemoteException {
        for (Map.Entry<RMIYelpServerProtocol, Integer> otherVaultsEntry : vaults.entrySet()){
            // Notify other vaults of this new vaults existence.
            otherVaultsEntry.getKey().connectDataVault(vault, nodeN);
            // Notify the new vaults of other vaults in the system.
            vault.connectDataVault(otherVaultsEntry.getKey(), otherVaultsEntry.getValue());
        }
        //
        vaults.put(vault, nodeN);
        System.out.println("SERVER: Vault("+nodeN+") connected.");

    }

    @Override
    public void ping() {
    }

    public void makeAvailable(int regPort) {
        try {
//            System.setProperty("java.security.policy","file:src/security.policy");
//            if (System.getSecurityManager() == null) {
//                System.setSecurityManager(new SecurityManager());
//            }

            UnicastRemoteObject.exportObject(this, 0);
            LocateRegistry.createRegistry(regPort).rebind("RMIYelpServer", this);
            System.out.println("Server started. ");
        } catch(RemoteException e) {
            System.out.println("SERVER: Server remote exception " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        if (args.length == 1){
            RMIYelpServer server = new RMIYelpServer();
            server.makeAvailable(Integer.parseInt(args[0]));
        } else {
            System.out.println("Args are not correct.");
        }
    }
}
