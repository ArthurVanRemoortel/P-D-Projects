package com.vub.pdproject.search;

import com.vub.pdproject.Util;
import com.vub.pdproject.data.models.Review;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.RecursiveTask;
import java.util.HashMap;

public class ParallelSearchReviewsTask extends RecursiveTask<Integer> {
//    final int p;
    String searchString;
    ArrayList<Review> reviews;
    boolean parallelCountOccurrences = true;
    int first;
    int last;
    int T;

//    ParallelSearchReviewsTask(String searchString, String text){
//        this(searchString, text.split("\\s+"));
//    }

    ParallelSearchReviewsTask(String searchString, ArrayList<Review> reviews, int T){
        this(searchString, reviews, T, 0, reviews.size());
    }

    ParallelSearchReviewsTask(String searchString, ArrayList<Review> reviews, int T, int first, int last){
        this.T = T;
        this.searchString = searchString;
        this.reviews = reviews;
        this.first = first;
        this.last = last;
    }


    @Override
    protected Integer compute() {
        if (last - first < 2) {
            if (reviews.isEmpty())
                return 0;
            Review review = reviews.get(first);
            return countOccurrences(searchString, review.text);
        } else if (last - first <= T) {
//            Map<Review, Integer> result = new HashMap<>();
            int occurrences = 0;
            for(Review review : reviews.subList(first, last)){
                occurrences += countOccurrences(searchString, review.text);
            }
            return occurrences;
        } else {
            int pivot = (first + last) / 2;
            ParallelSearchReviewsTask left_task = new ParallelSearchReviewsTask(searchString, reviews, T, first, pivot);
            ParallelSearchReviewsTask right_task = new ParallelSearchReviewsTask(searchString, reviews, T, pivot, last);
            right_task.fork();
            int left_res = left_task.compute();
            int right_res = right_task.join();
            return left_res + right_res;
        }
    }

    // Copied from SequentialSearch
    public int countOccurrences(String keyword, String text){
        // parallelCountOccurrences == true means the code for faze 2 will be used instead of the linear version from faze 1.
        if (parallelCountOccurrences) {
            return getPool().invoke(new ParallelCountOccurrencesTask(keyword, text, T));
        } else {
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
}
