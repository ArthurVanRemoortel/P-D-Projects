package com.vub.pdproject.data.readers;

import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vub.pdproject.data.models.Business;
import com.vub.pdproject.data.models.Review;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Sam van den Vonder
 */
public class YelpReviewReader {

    /**
     * Read a file of Yelp review data and return in-memory objects mapped to their ID
     *
     * @param dataPath Path to the Yelp JSON review data, e.g. "data/yelp_academic_dataset_review.json"
     * @return A mapping from review id's to reviews
     */
    static public Map<String, Review> readData(String dataPath) throws IOException {
        // Read all reviews
        InputStream dataStream = getInputStream(dataPath);
        return readJsonStream(dataStream);
    }

    /**
     *
     * @param dataPath Path to the Yelp JSON review data, e.g. "data/yelp_academic_dataset_review.json"
     * @param businesses A collection of businesses for whom you want to retrieve review data
     * @return A mapping from review id's to reviews
     * @throws IOException
     */
    static public Map<String, Review> readForBusinesses(String dataPath, Map<String, Business> businesses) throws IOException {
        // Read all reviews
        InputStream dataStream = getInputStream(dataPath);
        return readJsonStream(dataStream, review -> businesses.containsKey(review.businessId));
    }


    /**
     * @param inputStream A stream of data
     * @return A mapping from id to reviews, converted from entries on the stream of data
     */
    static private Map<String, Review> readJsonStream(InputStream inputStream) throws IOException {
        return readJsonStream(inputStream, review -> true);
    }


    /**
     * @param inputStream A stream of data
     * @param shouldInclude A lambda that determines whether a particular review should be included in the result set
     * @return A mapping from id to reviews, converted from entries on the stream of data
     */
    static private Map<String, Review> readJsonStream(InputStream inputStream, Function<Review, Boolean> shouldInclude) throws IOException {
        Gson gson = createBuilder();
        Reader reader = new InputStreamReader(inputStream, "UTF-8");
        JsonStreamParser parser = new JsonStreamParser(reader);

        HashMap<String, Review> reviews = new HashMap<>();
        while (parser.hasNext()) {
            JsonElement e = parser.next();
            if (e.isJsonObject()) {
                Review review = gson.fromJson(e, Review.class);
                if (shouldInclude.apply(review))
                    reviews.put(review.id, review);
            }
        }
        reader.close();
        return reviews;
    }


    /**
     * Create a builder for JSON data
     *
     * @return a GSON builder for JSON data
     */
    static private Gson createBuilder() {
        GsonBuilder builder = new GsonBuilder();
        return builder.create();
    }

    /**
     * Given a path to a file, turn it into a stream of data
     *
     * @param dataPath Path to a file
     * @return A stream of data
     */
    static private InputStream getInputStream(String dataPath) throws FileNotFoundException {
        return new FileInputStream(dataPath);
    }
}