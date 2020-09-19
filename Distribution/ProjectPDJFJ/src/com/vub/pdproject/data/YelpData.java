package com.vub.pdproject.data;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.vub.pdproject.data.models.Business;
import com.vub.pdproject.data.models.Review;
import com.vub.pdproject.data.readers.YelpBusinessReader;
import com.vub.pdproject.data.readers.YelpDataHelper;
import com.vub.pdproject.data.readers.YelpReviewReader;
import com.vub.pdproject.data.readers.YelpBusinessReader.Cities;

/**
 * 
 * Class representing a subset of Yelp data.
 * 
 * @author Steven Adriaensen (Updated by Jens Van der Plas)
 *
 */
public class YelpData {
	private static final String preset_loc = "data/presets"; //root folder containing data presets
	private static final String all_reviews_loc = "data/yelp_academic_dataset_review.json"; //path to Yelp review file
	private static final String all_businesses_loc = "data/yelp_academic_dataset_business.json"; //path to Yelp business file
	
	public Map<String, Business> businesses; //businesses data: Mapping from business IDs to their data
	public Map<String, Review> reviews; //reviews map: Mapping from review IDs to their data
	private final List<String> businessIDs; //a list of all business IDs
	
	
	public YelpData(Map<String, Business> businesses, Map<String, Review> reviews){
		this.businesses = businesses;
		this.reviews = reviews;
		this.businessIDs = new ArrayList<String>(businesses.keySet());
	}

	// Lazy way to create a pseudo-unique id. Assumes no two reviews will be created at the same time.
	private String generateUniqueID(){
		Date date = new Date();
		long timeMilli = date.getTime();
		return Long.toString(timeMilli);
	}
	
	/**
	 * @return a list of all business IDs (in O(1))
	 */
	public List<String> getBusinessIDs(){
		return businessIDs;
	}

	/**
	 * @param bid business ID
	 * @return data for business with ID
	 */
	public Business getBusiness(String bid){
		return businesses.get(bid);
	}
	
	/**
	 * @param rid review ID
	 * @return data for review with ID
	 */
	public Review getReview(String rid){
		return reviews.get(rid);
	}

	public String toString(){
		long shortest_review = Integer.MAX_VALUE;
		long longest_review = -1;
		long total_length_reviews = 0;
		long least_reviews = Integer.MAX_VALUE;
		long most_reviews = -1;
		long total_reviews = 0;
		for(Business bd: businesses.values()){
			long nreviews = bd.reviews.size();
			least_reviews = Math.min(least_reviews, nreviews);
			most_reviews = Math.max(most_reviews, nreviews);
			total_reviews += nreviews;
			for(String rid : bd.reviews){
				long rlength = reviews.get(rid).text.length();
				shortest_review = Math.min(shortest_review, rlength);
				longest_review = Math.max(longest_review, rlength);
				total_length_reviews += rlength;
			}
		}
		return "# businesses: "+businesses.size()+System.lineSeparator()
				+ "# reviews: "+total_reviews+System.lineSeparator()
				+ "# characters: "+total_length_reviews+System.lineSeparator()
				+ "avg. # reviews per business: "+(double)total_reviews/businesses.size() + " (min: "+least_reviews+", max: "+most_reviews+")"+System.lineSeparator()
				+ "avg. # characters per review: "+(double)total_length_reviews/total_reviews + " (shortest: "+shortest_review+", longest: "+longest_review+")";
	}
	
	/**
	 * @param index The index of the data preset to load.
	 * @return the data preset with given index.
	 */
	static public YelpData forPreset(int index){
		try {
			Map<String, Business> businesses = YelpBusinessReader.readData(preset_loc+"/"+index+"/businesses.json");
			Map<String, Review> reviews = YelpReviewReader.readForBusinesses(preset_loc+"/"+index+"/reviews.json", businesses);
            return new YelpData(businesses,reviews);
        }catch (IOException e){
            e.printStackTrace();
        }
		return null;
	}

	/**
	 * @return The data from the dataset on Serenity.
	 */
	static public YelpData forSerenity(){
		try {
			Map<String, Business> businesses = YelpBusinessReader.readData("/data/PD/businesses.json");
			Map<String, Review> reviews = YelpReviewReader.readForBusinesses("/data/PD/reviews.json", businesses);
			return new YelpData(businesses,reviews);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Returns the full Yelp dataset. 
	 * 
	 * Beware:
	 * - The full data set is not distributed as part of the project (as it is over 4GB), but can be found on 
	 *   https://www.yelp.com/dataset_challenge
	 * - As this method reads the whole dataset into memory, this method requires a heapsize of at least 8 gig.
	 * 
	 * @return The full Yelp data-set.
	 * @throws YelpDataMissingException
	 */
	static public YelpData full() throws YelpDataMissingException{
		yelp_data_present();
		try {
			Map<String, Business> businesses = YelpBusinessReader.readData(all_businesses_loc);
            return new YelpData(businesses,loadReviews(businesses));
        }catch (IOException e){
            e.printStackTrace();
        }
		return null;
	}
	
	/**
	 * Returns the Yelp dataset for a given city.
	 * Hint: Cities.Las_Vegas has the most listed businesses/reviews.
	 * 
	 * Beware:
	 * - The full data set is not distributed as part of the project (as it is over 4 GB), but can be found on 
	 *   https://www.yelp.com/dataset_challenge
	 *   
	 * @return The full Yelp data-set.
	 * @throws YelpDataMissingException
	 */
	static public YelpData forCity(Cities city) throws YelpDataMissingException{
		yelp_data_present();
		try {
			Map<String, Business> businesses = YelpBusinessReader.readCityData(all_businesses_loc, city);
            return new YelpData(businesses,loadReviews(businesses));
        }catch (IOException e){
            e.printStackTrace();
        }
		return null;
	}

	
	/**
	 * A convenience method to test 'count occurrences'.
	 * 
	 * @param name		Name of single business
	 * @param stars		Review rating of single business
	 * @param text		Text of the single review 
	 * @return 			A dataset with a single business with given name, having a single review with given text and rating
	 */
	static public YelpData forReviewText(String name, int stars, String text){
		final String bid = "b1";
		final String rid = "r1";
		
		//generate single business
		Map<String, Business> businesses = new HashMap<String, Business>();
		Business bd = new Business();
		bd.name = name;
		bd.id = bid;
		bd.address = "some address";
		bd.city = "some city";
		bd.reviews = Arrays.asList(rid);
		bd.stars = stars;
		businesses.put(bid,bd);
		
		//generate single review
		Map<String, Review> reviews = new HashMap<String, Review>();
		Review rd = new Review();
		rd.businessId = bid;
		rd.id = rid;
		rd.stars = stars;
		rd.text = text;
		reviews.put(rid,rd);

		return new YelpData(businesses,reviews);
	}
	
	
	static private Map<String, Review> loadReviews(Map<String, Business> businesses) throws IOException{
		Map<String, Review> reviews = YelpReviewReader.readForBusinesses(all_reviews_loc, businesses);
        YelpDataHelper.populateBusinessReviews(businesses, reviews);
        return reviews;
	}
	
	static private void yelp_data_present() throws YelpDataMissingException{
		if(!new File(all_businesses_loc).exists()||new File(all_reviews_loc).exists()){
			throw new YelpDataMissingException();
		}
	}
	
	/**
	 * Exception indicating that the businesses/reviews files from the Yelp data-set weren't found. 
	 * 
	 * @author Steven Adriaensen
	 */
	@SuppressWarnings("serial")
	static public class YelpDataMissingException extends Exception{
		YelpDataMissingException(){
			super(all_businesses_loc + " or " + all_reviews_loc + " doesn't exist."
					+ "If you wish to use this function, "
					+ "1) download the data from https://www.yelp.com/dataset_challenge"
					+ "2) place the review/business files in the locations indicated above"
					+ "(or update file references in YelpData.java accordingly)");
		}
	}

}
