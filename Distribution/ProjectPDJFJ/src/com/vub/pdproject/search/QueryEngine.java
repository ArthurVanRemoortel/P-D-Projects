package com.vub.pdproject.search;
import java.io.Serializable;
import java.util.List;

import com.vub.pdproject.data.YelpData;


public interface QueryEngine {
	/**
	 * Searches a given (yelp) dataset, for businesses relevant for a given keyword.
	 * 
	 * @param keyword: A single word (not containing any white space characters)
	 * @param data: The subset of businesses/reviews to be searched.
	 * @return A list of businesses and their non-zero relevance, sorted by decreasing relevance.
	 */
	List<RRecord> search(String keyword, YelpData data); 
	
	/*
	 * Record storing a business ID alongside its relevance.
	 * Its natural order is by decreasing relevance. 
	 */
	static class RRecord  implements Comparable<RRecord>, Serializable {
		public String businessID;
		public double relevance_score;
		
		RRecord(String businessID, double relevance_score){
			this.businessID = businessID;
			this.relevance_score = relevance_score;
		}
		
		@Override
		public int compareTo(RRecord other) {
			return Double.compare(other.relevance_score,relevance_score);
		}
	}
}
