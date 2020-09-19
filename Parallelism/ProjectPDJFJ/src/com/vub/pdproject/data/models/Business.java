package com.vub.pdproject.data.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

//(Partial) data model for a business listed by Yelp
public class Business {
    @SerializedName("business_id")
    public String id; //a unique identifier for a business
    public String name; //the name of the business
    public String address; //the address of the business
    public String city; //the name of the city in which the business is located
    public float stars; //the average review score (1-5 stars)

    // Not included in the JSON - needs to be populated afterwards
    public List<String> reviews = new ArrayList<>(); //a list of IDs of the reviews available for this business

    @Override
    public String toString() {
        return "Business{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", stars=" + stars +
                '}';
    }
}
