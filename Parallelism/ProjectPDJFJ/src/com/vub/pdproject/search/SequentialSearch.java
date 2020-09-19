package com.vub.pdproject.search;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vub.pdproject.Util;
import com.vub.pdproject.data.YelpData;
import com.vub.pdproject.data.models.Business;

/**
 * Sequential implementation of QueryEngine
 * 
 * @author Steven Adriaensen
 *
 */
public class SequentialSearch implements QueryEngine{

	@Override
	public List<RRecord> search(String keyword, YelpData data) {
		//loop over businesses, determine their relevance & add them to a list if relevance > 0
		List<RRecord> relevant_businesses = new ArrayList<RRecord>();
		for(String bid : data.getBusinessIDs()){
			double relevance = evaluate_relevance(keyword,bid,data);
			if(relevance > 0){
				relevant_businesses.add(new RRecord(bid,relevance));
			}
		}
		//sort the resulting list by decreasing relevance
		Collections.sort(relevant_businesses);
		return relevant_businesses;
	}
	
	/**
	 * @param keyword		A single keyword (without spaces)
	 * @param businessID	The id of the business whose relevance will be determined
	 * @param data			The data to be searched
	 * @return 				The relevance of given business for a given keyword
	 */
	public static double evaluate_relevance(String keyword, String businessID, YelpData data) {
		//fetch data for business
		Business bd = data.getBusiness(businessID);
		
		//check in how many times query string appears in reviews
		int occurences = 0;
		for(String rid : bd.reviews){
			occurences += countOccurrences(keyword,data.getReview(rid).text);
		}
		
		//calculate relevance score
		double relevance_score = 0;
		if(countOccurrences(keyword,bd.name) > 0){
			relevance_score = 0.5;
		}
		relevance_score += 1.5*occurences/(occurences+20);
		relevance_score *= bd.stars;
		return relevance_score;
	}
	
	/**
	 * 
	 * @param keyword	The keyword to be searched for
	 * @param text 		The text to be searched.
	 * @return 			The number of occurrences of a keyword in a text
	 * 					Search is case-sensitive. 
	 * 					To match, a word in text must be delimited by white space/punctuation marks 
	 *                  (or appear at the beginning/end of the text)
	 * 
	 * 					examples:
	 * 
	 * 					keyword: burger
	 * 
	 * 					text: "This burger was so good!" (returns 1)
	 * 					text: "Great burger" (returns 1)
	 * 					text: "Great burger!" (returns 1)
	 * 					text: "Great Burger" (returns 0)
	 * 					text: "burgers don't get any better!" (returns 0)
	 * 
	 */
	public static int countOccurrences(String keyword, String text){
	    int count = 0;
	    int k = 0;
	    for (int i=0; i < text.length(); i++){
	    	if(Util.isWhitespaceOrPunctuationMark(text.charAt(i))){
	    		if(k == keyword.length()){
	            	count++;
	            }
	    		k = 0;
	    	}else if(k >= 0){
		        if(k < keyword.length() && text.charAt(i) == keyword.charAt(k)){
		            k++;
		        }else{
		        	k = -1;
		        }
	    	}
	    }
	    if(k == keyword.length()){
	    	count++;
        }
	    return count;
	}
	
}
