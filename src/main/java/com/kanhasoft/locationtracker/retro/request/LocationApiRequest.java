package com.kanhasoft.locationtracker.retro.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationApiRequest {

    @SerializedName("lat")
    @Expose
    private double latitude;

    @SerializedName("long")
    @Expose
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "LocationApiRequest{" +
                "latitude='" + latitude + '\'' +
                ", longitude=" + longitude +
                '}';
    }
}
