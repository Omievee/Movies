package com.mobile.model;


import android.os.Parcelable;

import org.parceler.Parcel;

import java.io.Serializable;
import java.util.ArrayList;

@Parcel
public class Theater implements Serializable {

    int id;
    int moviepassId;
    int tribuneTheaterId;
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

    public Theater() {
    }

    public String getName() {
        if (name != null) {
            return name;
        }
        return "";
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
