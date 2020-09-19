package com.vub.pdproject.search;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.vub.pdproject.Util;
import com.vub.pdproject.data.YelpData;
import com.vub.pdproject.data.models.Business;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ParallelCountOccurrencesTask extends RecursiveTask<Integer> {
//    final int p;
    String searchString;
    String[] words;
    int first;
    int last;
    int T;

    ParallelCountOccurrencesTask(String searchString, String text, int T){
        this(searchString, text.split("\\s+"), T);
    }

    ParallelCountOccurrencesTask(String searchString, String[] words, int T){
        this.searchString = searchString;
        this.words = words;
        this.first = 0;
        this.last = words.length;
        this.T = T;
    }

    ParallelCountOccurrencesTask(String searchString, String[] words, int T, int first, int last){
        this.searchString = searchString;
        this.words = words;
        this.first = first;
        this.last = last;
        this.T = T;
    }


    @Override
    protected Integer compute() {
        if (last - first < 2) {
            return countOccurrences(searchString, words[first]);
        } else if (last - first <= T) {
            String[] wordsRegion = Arrays.copyOfRange(words, first, last);
            int occurrences = 0;
            for (String word: wordsRegion)
                occurrences += countOccurrences(searchString, word);
            return occurrences;
        } else {
            int pivot = (first + last) / 2;
            //System.out.println("Splitting: "+first + " -> "+last);
            ParallelCountOccurrencesTask left_task = new ParallelCountOccurrencesTask(searchString, words, T, first, pivot);
            ParallelCountOccurrencesTask right_task = new ParallelCountOccurrencesTask(searchString, words, T, pivot, last);
            right_task.fork();
            Integer left_res = left_task.compute();
            Integer right_res = right_task.join();
            return left_res + right_res;
        }
    }

    // Copied from SequentialSearch
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
