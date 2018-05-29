package com.mobile.model;


import android.support.annotation.Nullable;

import org.parceler.Parcel;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@Parcel(value = Parcel.Serialization.BEAN, analyze = {Theater.class})
@RealmClass
public class Theater extends RealmObject implements Serializable {

    @PrimaryKey
    int id;




    String name;
    String address;
    String city;
    String state;
    String zip;
    double distance;
    double lon;
    double lat;
    String theaterChainName;
    String ticketType;
    String synopsis;
    int moviepassId;
    int tribuneTheaterId;

    public void setId(int id) {
        this.id = id;
    }

    public void setMoviepassId(int moviepassId) {
        this.moviepassId = moviepassId;
    }

    public void setTribuneTheaterId(int tribuneTheaterId) {
        this.tribuneTheaterId = tribuneTheaterId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setTheaterChainName(String theaterChainName) {
        this.theaterChainName = theaterChainName;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }


    public Theater() {
    }

    @Nullable public String getName() {
        return name;
    }

    public boolean ticketTypeIsStandard() {
        return ticketType.matches("STANDARD");
    }

    public boolean ticketTypeIsETicket() {
        return ticketType.matches("E_TICKET");
    }

    public boolean ticketTypeIsSelectSeating() {
        return ticketType.matches("SELECT_SEATING");
    }

    public String getTicketType() {
        return ticketType;
    }

    public String getAddress() {
        return address;
    }

    public double getDistance() {
        return distance;
    }

    public int getMoviepassId() {
        return moviepassId;
    }

    public int getTribuneTheaterId() {
        return tribuneTheaterId;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return getName();
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZip() {
        return zip;
    }


}
