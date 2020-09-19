package com.vub.pdproject.data.readers;

import com.vub.pdproject.data.models.Business;
import com.vub.pdproject.data.models.Review;

import java.util.Map;

/**
 * @author Sam van den Vonder
 */
public class YelpDataHelper {
	
    /**
     * Populate the "reviews" field in every business to contain a list of all review ids that reference the business
     *
     * @param businesses the businesses to populate
     * @param reviews    the reviews to populate the businesses with
     * @return a mapping of business ids to businesses, where the business 'reviews' data has been populated.
     * Note that businesses are destructively modified.
     */
    static public Map<String, Business> populateBusinessReviews(Map<String, Business> businesses,
                                                                    Map<String, Review> reviews) {
        for (Map.Entry<String, Review> entry : reviews.entrySet()) {
            String reviewId = entry.getKey();
            Review review = entry.getValue();

            String businessId = review.businessId;
            Business business = businesses.get(businessId);

            business.reviews.add(reviewId);
        }
        return businesses;
    }
}
