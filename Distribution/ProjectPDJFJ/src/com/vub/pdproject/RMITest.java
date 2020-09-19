package com.vub.pdproject;

import com.vub.pdproject.data.models.Business;
import com.vub.pdproject.data.models.Review;
import com.vub.pdproject.search.QueryEngine;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.*;


public class RMITest {

    private static final ArrayList<RMIDataVault> vaults = new ArrayList<>();
    private static RMIYelpServer server;
    private static RMIYelpClient client;

    @BeforeClass
    public static void setupRMI() throws IOException {
        int PORT = 0;
        int REG_PORT = 1234;

        server = new RMIYelpServer();
        server.makeAvailable(REG_PORT);

        int TOTAL_NODES = 3;
        int MAX_BUSINESSES = 3;
        int MAX_REVIEWS = 4;

        for (int nodeN = 1; nodeN <= TOTAL_NODES; nodeN++){
            String review_loc = "data/presets/demonstration/reviewsVault"+nodeN+".json";
            String businesses_loc = "data/presets/demonstration/businessesVault"+nodeN+".json";
            RMIDataVault vault = new RMIDataVault(nodeN, TOTAL_NODES, MAX_BUSINESSES, MAX_REVIEWS, businesses_loc, review_loc);
            vault.initialize(REG_PORT);
            vaults.add(vault);
        }

        client = new RMIYelpClient();
        client.initialize(REG_PORT);

    }

    @Test
    public void testAddBusiness() throws RemoteException {
        // Tests is a newly added business can be retrieved from the server.
        Business added_business = client.addBusiness("testAddBusiness", "Pleinlaan 9", "Brussel");
        Business business_expected = client.getBusiness(added_business.id);
        assertEquals(business_expected, added_business);
    }


    @Test
    public void testAddReview() throws RemoteException {
        // Tests if reviews added bu a client can be retrieved from the server.
        String randomBusinessID = server.getAllBusinesses().values().iterator().next().id;
        randomBusinessID = "fUqTs96_C5xJWnYpYOnzjA";  // TODO: This is for demo only. Remove this later.
        System.out.println("randomBusinessID = " + randomBusinessID);
        Business randomBusiness = client.getBusiness(randomBusinessID);
        Review added_review = client.addReview(randomBusiness.id, "testAddReview", 5);

        // Retrieve the business again from the server to make sure the reviews was stored there.
        Business business_expected = client.getBusiness(randomBusinessID);
        ArrayList<Review> reviews_expected = client.getReviews(business_expected.reviews);
        assertTrue(reviews_expected.contains(added_review));
    }

    @Test
    public void testSearch() throws RemoteException {
        // Tests if newly added company shows up in the com.vub.pdproject.search results.
        Business added_business = client.addBusiness("arthur shop", "Pleinlaan 9", "Brussel");
        Review added_review = client.addReview(added_business.id, "This is a review for arthur shop.", 5);

        added_business = client.addBusiness("arthur industries", "Pleinlaan 9", "Brussel");
        added_review = client.addReview(added_business.id, "This is a review.", 2);

        Map<QueryEngine.RRecord, Business> results = client.search("arthur");
        assertTrue(results.containsValue(added_business));
    }
}
