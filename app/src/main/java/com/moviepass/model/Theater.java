package com.moviepass.model;

import org.parceler.Parcel;

/**
 * Created by anubis on 6/7/17.
 */

@Parcel
public class Theater {

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

    public Theater() {
    }

    public String getName() {
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

    public int getMoviepassId() { return  moviepassId; }

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

    public String getCity() { return city; }

    public String getState() { return state; }

    public String getZip() { return zip; }

}
