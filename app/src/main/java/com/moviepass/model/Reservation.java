package com.moviepass.model;

/**
 * Created by anubis on 6/20/17.
 */

public class Reservation {

    int id;
    long expiration;
    String qrUrl;
    String seat;
    String confirmationCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public String getQrUrl() {
        return qrUrl;
    }

    public void setQrUrl(String qrUrl) {
        this.qrUrl = qrUrl;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

}