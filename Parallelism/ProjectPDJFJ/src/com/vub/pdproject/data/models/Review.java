package com.vub.pdproject.data.models;

import com.google.gson.annotations.SerializedName;

//(Partial) data model for a user review on Yelp
public class Review {
    @SerializedName("review_id")
    public String id; //a unique identifier for this review
    @SerializedName("business_id")
    public String businessId; //the id of the reviewed business
    public int stars; //the review rating (1-5 stars)
    public String text; //the review text
}
