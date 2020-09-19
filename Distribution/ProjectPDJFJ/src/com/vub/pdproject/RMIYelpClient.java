package com.vub.pdproject;

import com.vub.pdproject.data.models.Business;
import com.vub.pdproject.data.models.Review;
import com.vub.pdproject.search.QueryEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RMIYelpClient implements RMIYelpServerProtocol {

    private RMIYelpServerProtocol server;
    private Map<QueryEngine.RRecord, Business> lastSearchResults;
    private Business selectedBusiness;

    public RMIYelpClient(){
    }

    @Override
    public Map<QueryEngine.RRecord, Business> search(String search_string) throws RemoteException {
        Map<QueryEngine.RRecord, Business> results = server.search(search_string);
        lastSearchResults = results;
        return results;
    }

    @Override
    public Business addBusiness(String name, String address, String city) throws RemoteException {
        return server.addBusiness(name, address, city);
    }

    @Override
    public Business getBusiness(String businessID) throws RemoteException {
        return server.getBusiness(businessID);
    }

    @Override
    public Review addReview(String businessID, String reviewText, int reviewStars) throws RemoteException {
        return server.addReview(businessID, reviewText, reviewStars);
    }

    @Override
    public Review addRemoteReview(Review review) throws RemoteException {
        return null;
    }

    @Override
    public ArrayList<Review> getReviews(List<String> reviewIDs) throws RemoteException {
        return server.getReviews(reviewIDs);
    }

    @Override
    public Map<String, Review> getAllReviews() throws RemoteException {
        return null;
    }

    @Override
    public Map<String, Business> getAllBusinesses() throws RemoteException {
        return null;
    }

    public void processUserInput(){
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Type a message: ");

        try {
            String userInput = stdIn.readLine();
            while (userInput != null) {
                String[] command = userInput.split(" ");

                switch (command[0]) {
                    case "select":
                        int index = Integer.parseInt(command[1]);
                        ArrayList<Business> l = new ArrayList<Business>(lastSearchResults.values());
                        selectedBusiness = l.get(index);
                        System.out.println("You have selected: " + selectedBusiness.name);
                        break;
                    case "search":
                        Map<QueryEngine.RRecord, Business> results = search(command[1]);
                        System.out.println("Result: " + results.size() + " for " + command[1]);
                        int i = 0;
                        for(Map.Entry<QueryEngine.RRecord, Business> bdEntry : results.entrySet()){
                            System.out.println(i+") "+bdEntry.getValue().name+" ("+bdEntry.getKey().relevance_score+")");
                            i++;
                        }
                        break;
                    case "addbusiness":
                        System.out.print("Business name: ");
                        String name = stdIn.readLine();
                        System.out.print("Address: ");
                        String address = stdIn.readLine();
                        System.out.print("city: ");
                        String city = stdIn.readLine();
                        selectedBusiness = addBusiness(name, address, city);
                        if (selectedBusiness != null){
                            System.out.println("Created: " + selectedBusiness);
                            System.out.println("You have selected: " + selectedBusiness.name);
                        } else {
                            System.out.println("ERROR: Business could not be added.");
                        }

                        break;
                    case "getreviews":
                        selectedBusiness = getBusiness(selectedBusiness.id); // Business might have changed on the server.
                        List<Review> reviews = getReviews(selectedBusiness.reviews);
                        for (Review review : reviews) {
                            System.out.println(review.stars + "/" + "5");
                            System.out.println(review.text);
                            System.out.println();
                        }
                        break;
                    case "addreview":
                        System.out.print("Review text: ");
                        String reviewText = stdIn.readLine();
                        System.out.print("Stars: ");
                        int reviewStars = Integer.parseInt(stdIn.readLine());
                        Review review = addReview(selectedBusiness.id, reviewText, reviewStars);
                        if (review == null)
                            System.out.println("ERROR: Business could not be added.");
                        break;
                    default:
                        System.out.println("Unknown command: " + command[0]);
                        break;

                }
                System.out.print("Type a message: ");
                userInput = stdIn.readLine();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize(int regPort) {
        try {
//            System.setProperty("java.security.policy","file:src/security.policy");
//            if (System.getSecurityManager() == null) {
//                System.setSecurityManager(new SecurityManager());
//            }
            Registry reg = LocateRegistry.getRegistry("localhost", regPort);
            server = (RMIYelpServerProtocol) reg.lookup("RMIYelpServer");
            RMIYelpServerProtocol stub = (RMIYelpServerProtocol) UnicastRemoteObject.exportObject(this, 0);
            System.out.println("Finished init");

        } catch(RemoteException e) {
            System.out.println("Client remote exception: " + e.getMessage());
        } catch(NotBoundException e) {
            System.out.println("Client not bound exception: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length == 1){
            RMIYelpClient client = new RMIYelpClient();
            client.initialize(Integer.parseInt(args[0]));
            client.processUserInput();
        } else {
            System.out.println("Args are not correct.");
        }
    }

    // Should not be called by client.
    @Override
    public void connectDataVault(RMIYelpServerProtocol vault, Integer nodeN) throws RemoteException {
    }

    @Override
    public void ping() throws RemoteException {
    }

}
