package com.vub.pdproject.search;

import java.util.*;

import com.vub.pdproject.Util;
import com.vub.pdproject.data.YelpData;
import com.vub.pdproject.data.models.Business;
import com.vub.pdproject.data.models.Review;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * TODO: A parallel implementation of QueryEngine using Java Fork-join
 * (see assignment for detailed requirements of this implementation)
 * 
 * This is normally the only file you should change (except for Main.java for testing/evaluation).
 * If you for some reason feel the need to change another existing file, 
 * contact Steven Adriaensen first, and mention this modification explicitly in the report.
 * Note that adding new files for modularity purposes is always ok.
 * 
 * @author You
 *
 */
public class ParallelSearch implements QueryEngine{
	final int p; //parallelism level (i.e. max. # cores that can be used by Java Fork/Join)
	final int T; //sequential threshold (semantics depend on your cut-off implementation)

	ForkJoinPool searchPool;

	/**
	 * Creates a parallel search engine with p worker threads.
	 * Counting occurrences is to be done sequentially (T ~ +inf)
	 * 
	 * @param p parallelism level
	 */
	ParallelSearch(int p){
		this(p,Integer.MAX_VALUE);
	}
	
	/**
	 * Creates a parallel search engine with p worker threads and sequential cut-off threshold T.
	 * 
	 * @param p parallelism level
	 * @param T sequential threshold
	 */
	public ParallelSearch(int p, int T){
		this.p = p;
		this.T = T;
		//System.out.println("*** EXECUTING/PARTITIONING WORK ***");
		//System.out.println("Execution using "+p+" parallel thread(s)...");
		searchPool = new ForkJoinPool(p);

	}


	@Override
	public List<RRecord> search(String query_str, YelpData data) {
		//TODO: implement this method using Java Fork-Join
		//ForkJoinPool pool = new ForkJoinPool();
		List<RRecord> r = searchPool.invoke(new SearchTask(data, query_str, T));
		return r;
	}
}



class SearchTask extends RecursiveTask<List<QueryEngine.RRecord>> {
	private static final long serialVersionUID = 1L;

	YelpData data;
	String query_str;
	int first;
	int last;
	int T;


	SearchTask(YelpData data, String query_str, int T) {
		this(data, query_str, T, 0, data.getBusinessIDs().size());
	}


	SearchTask(YelpData data, String query_str, int T, int first, int last) {
		this.data = data;
		this.query_str = query_str;
		this.T = T;
		this.first = first;
		this.last = last;
	}

	@Override
	protected List<QueryEngine.RRecord> compute() {
		if (last - first < 2) {
			String bid = data.getBusinessIDs().get(first);
			double relevance = evaluate_relevance(query_str, bid, data);
			if(relevance > 0){
				return Collections.singletonList(new QueryEngine.RRecord(bid,relevance));
			} else {
				return new ArrayList<>();
			}
		} else if (last - first <= T) {
			List<QueryEngine.RRecord> relevant = computeRelevant(data, query_str, first, last);
			Collections.sort(relevant);
			return relevant;
		} else {
			int pivot = (first + last) / 2;
			SearchTask left_task = new SearchTask(data, query_str, T, first, pivot);
			SearchTask right_task = new SearchTask(data, query_str, T, pivot, last);
			right_task.fork();
			List<QueryEngine.RRecord> left_res = new ArrayList<>(left_task.compute());
			List<QueryEngine.RRecord> right_res = right_task.join();
			//left_res.addAll(right_res);
			return merge(left_res, right_res);
		}
		//return sortingArray;
	}

	// Modified from https://dehasi.github.io/java/2017/06/06/merge-sort-with-fork-join.html
	public static List<QueryEngine.RRecord> merge(List<QueryEngine.RRecord> a, List<QueryEngine.RRecord> b) {
		int i=0, j=0;
		List<QueryEngine.RRecord> result = new ArrayList<>(a.size() + b.size());
		while(i < a.size() && j < b.size())
			result.add((a.get(i).compareTo(b.get(j)) < 0) ? a.get(i++): b.get(j++));
		while(i < a.size())
			result.add(a.get(i++));
		while(j < b.size())
			result.add(b.get(j++));
		return result;
	}

	private List<QueryEngine.RRecord> computeRelevant(YelpData data, String query_str, int first, int last) {
		List<QueryEngine.RRecord> relevant_businesses = new ArrayList<QueryEngine.RRecord>();
		List<String> all_businesses = data.getBusinessIDs();
		for(String bid : all_businesses.subList(first, last)){
			double relevance = evaluate_relevance(query_str, bid, data);
			if(relevance > 0){
				relevant_businesses.add(new QueryEngine.RRecord(bid,relevance));
			}
		}
		return relevant_businesses;
	}

	// Copied from SequentialSearch
	public double evaluate_relevance(String keyword, String businessID, YelpData data) {
		Business bd = data.getBusiness(businessID);
		int occurrences = 0;

		ArrayList<Review> reviews = new ArrayList<>();
		for(String rid : bd.reviews){
			reviews.add(data.getReview(rid));
		}

		// TODO: Maybe use different T for ParallelSearchReviewsTask.
		// Trough testing I found that T/2 works best when using the serenity dataset.
		int review_occurences = getPool().invoke(new ParallelSearchReviewsTask(keyword, reviews, T/2));
		occurrences += review_occurences;

		double relevance_score = 0;
		if(countOccurrences(keyword, bd.name) > 0){
			relevance_score = 0.5;
		}
		relevance_score += 1.5*occurrences/(occurrences+20);
		relevance_score *= bd.stars;
		return relevance_score;
	}

	// Copied from SequentialSearch. Is only used to evaluate relevance of business name, not reviews.
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
