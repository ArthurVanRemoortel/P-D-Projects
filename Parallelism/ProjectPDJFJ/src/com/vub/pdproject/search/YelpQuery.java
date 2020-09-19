package com.vub.pdproject.search;

import java.util.List;

import com.vub.pdproject.data.YelpData;
import com.vub.pdproject.search.QueryEngine.RRecord;

/**
 * Convenience class to represent a whole query:
 * Combining both keyword and data to be searched
 * 
 * @author Steven Adriaensen
 *
 */
public class YelpQuery {
	private String keyword;
	private YelpData data;
	
	private YelpQuery(String keyword, YelpData data){
		this.keyword = keyword;
		this.data = data;
	}
	
	/**
	 * @return The keyword to be search for
	 */
	public String getKeyword(){
		return keyword;
	}
	
	/**
	 * @return The data to be searched
	 */
	public YelpData getData(){
		return data;
	}
	
	/**
	 * Executes this query, using a given query engine.
	 * 
	 * @param queryEngine: the query engine to be used to resolve this query
	 * @return the query result (business ids, their relevance, ordered by decreasing relevance)
	 */
	public List<RRecord> execute(QueryEngine queryEngine){
		return queryEngine.search(keyword, data);
	}
	
	public String toString(){
		return "keyword: "+keyword+System.lineSeparator()+""
				+ "data: "+System.lineSeparator()+""
				+ data;
	}
	
	/**
	 * Constructs the queries to be used to benchmark your application.
	 * 
	 * @param index: the index (identifier) of benchmark, also corresponds to the index of the data preset used.
	 * @return benchmark with given index
	 */
	public static YelpQuery forBenchmark(int index){
		if(index == 1){
			return new YelpQuery("Schnitzel",YelpData.forPreset(index));
		}else if(index == 2){
			return new YelpQuery("burger",YelpData.forPreset(index));
		}else if(index == 3) {
			return new YelpQuery("the",YelpData.forPreset(index));
		}else if(index == 0) {
			return new YelpQuery("the",YelpData.forSerenity());
		}else{
			System.err.println("Benchmark "+index+" does not exist.");
			return null;
		}
	}
}
