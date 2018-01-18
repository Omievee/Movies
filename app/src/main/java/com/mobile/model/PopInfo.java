package com.mobile.model;

/**
 * Created by anubis on 7/17/17.
 */

public class PopInfo {

    int reservationId;
    String movieTitle;
    String theaterName;
    String tribuneTheaterId;
    String showtime;
    String tribuneMovieId;

    public PopInfo() {
    }

    public String getMovieTitle() { return movieTitle; }

    public String getTribuneMovieId() { return tribuneMovieId; }

    public int getReservationId() { return reservationId; }

    public String getTheaterName() { return theaterName; }

    public String getTribuneTheaterId() { return tribuneTheaterId; }

    public String getShowtime() { return showtime; }
}
