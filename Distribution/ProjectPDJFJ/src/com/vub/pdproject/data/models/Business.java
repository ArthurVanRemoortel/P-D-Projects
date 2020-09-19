package com.vub.pdproject.data.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//(Partial) data model for a business listed by Yelp
public class Business implements Serializable {
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
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this, Business.class);
    }

    static public Business fromString(String businessString) throws InstantiationException {
        Gson gson = new GsonBuilder().create();

        JsonElement jElement = new JsonParser().parse(businessString);
        if (jElement.isJsonObject())
            return gson.fromJson(jElement, Business.class);
        else
            throw new InstantiationException("Cannot create instance from bad JSON: " + businessString);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        Business business = (Business) o;
        return business.id.equals(this.id);
    }
}
