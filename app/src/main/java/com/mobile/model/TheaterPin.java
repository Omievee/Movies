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
    public int theaterPinIcon;
    private double latitude;
    private double longitude;

    public boolean isTicketType() {
        return ticketType;
    }

    private boolean ticketType;
    private Theater mTheater;

    public TheaterPin() {
        mPosition = null;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setmSnippet(String mSnippet) {
        this.mSnippet = mSnippet;
    }

    public void setmArrayPosition(int mArrayPosition) {
        this.mArrayPosition = mArrayPosition;
    }

    public void setTheaterPinIcon(int theaterPinIcon) {
        this.theaterPinIcon = theaterPinIcon;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setmTheater(Theater mTheater) {
        this.mTheater = mTheater;
    }

    public TheaterPin(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    public TheaterPin(double lat, double lng, String title, int pictureResource, int arrayPosition, Theater theater) {
        latitude = lat;
        longitude = lng;
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        theaterPinIcon = pictureResource;
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
        return mTheater.getAddress() + ", " + mTheater.getCity() + " " + mTheater.getState();
    }



    public int getArrayPosition() {
        return mArrayPosition;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Theater getTheater() {
        return mTheater;
    }
}