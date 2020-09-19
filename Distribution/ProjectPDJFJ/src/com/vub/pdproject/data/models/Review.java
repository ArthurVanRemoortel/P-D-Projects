package com.vub.pdproject.data.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

//(Partial) data model for a user review on Yelp
public class Review implements Serializable {
    @SerializedName("review_id")
    public String id; //a unique identifier for this review
    @SerializedName("business_id")
    public String businessId; //the id of the reviewed business
    public int stars; //the review rating (1-5 stars)
    public String text; //the review text

    public String toString() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this, Review.class);
    }

    static public Review fromString(String reviewString) throws InstantiationException {
        Gson gson = new GsonBuilder().create();

        JsonElement jElement = new JsonParser().parse(reviewString);
        if (jElement.isJsonObject())
            return gson.fromJson(jElement, Review.class);
        else
            throw new InstantiationException("Cannot create review instance from bad JSON: " + reviewString);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        Review review = (Review) o;
        return review.id.equals(this.id);
    }
}
