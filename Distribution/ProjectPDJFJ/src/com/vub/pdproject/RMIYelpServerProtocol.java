package com.vub.pdproject;

import com.vub.pdproject.data.models.Business;
import com.vub.pdproject.data.models.Review;
import com.vub.pdproject.search.QueryEngine;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface RMIYelpServerProtocol extends Remote {

    public Map<QueryEngine.RRecord, Business> search(String search_string) throws RemoteException;

    public Business addBusiness(String name, String address, String city) throws RemoteException;

    public Business getBusiness(String businessID) throws RemoteException;

    public Review addReview(String businessID, String reviewText, int reviewStars) throws RemoteException;

    public Review addRemoteReview(Review review) throws RemoteException;

    public ArrayList<Review> getReviews(List<String> reviewIDs) throws RemoteException;

    public Map<String, Review> getAllReviews() throws RemoteException;

    public Map<String, Business> getAllBusinesses() throws RemoteException;

    public void connectDataVault(RMIYelpServerProtocol vault, Integer nodeN) throws RemoteException;

    public void ping() throws RemoteException;
}
