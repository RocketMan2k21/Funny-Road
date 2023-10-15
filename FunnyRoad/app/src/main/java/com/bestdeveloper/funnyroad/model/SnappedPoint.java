package com.bestdeveloper.funnyroad.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("jsonschema2pojo")
public class SnappedPoint {

    @SerializedName("location")
    @Expose
    private Location location;
    @SerializedName("originalIndex")
    @Expose
    private Integer originalIndex;
    @SerializedName("placeId")
    @Expose
    private String placeId;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Integer getOriginalIndex() {
        return originalIndex;

    }

    public void setOriginalIndex(Integer originalIndex) {
        this.originalIndex = originalIndex;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
}