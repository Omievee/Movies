package com.mobile.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by anubis on 6/29/17.
 */

public class TheaterPin implements ClusterItem {
    private final LatLng mPosition;
    private String mTitle;
    private String mSnippet;
    private int mArrayPosition;
    public int profilePhoto;
    private double latitude;
    private double longitude;
    private Theater mTheater;

    public TheaterPin(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    public TheaterPin(double lat, double lng, String title, int pictureResource, int arrayPosition, Theater theater) {
        latitude = lat;
        longitude = lng;
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        profilePhoto = pictureResource;
        mArrayPosition = arrayPosition;
        mTheater = theater;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }

    public int getArrayPosition() { return mArrayPosition; }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }

    public Theater getTheater() { return mTheater; }
}