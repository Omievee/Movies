package com.moviepass.responses;

/**
 * Created by o_vicarra on 12/20/17.
 */

public class ActiveReservationResponse {

    String reservationMovieTitle;
    String reservationTheater;
    String reservationConfirmationCode;
    String reservationTime;
    int reservationId;


    public ActiveReservationResponse(int reservationId) {
        this.reservationId = reservationId;
    }

    public String getReservationMovieTitle() {
        return reservationMovieTitle;
    }

    public String getReservationTheater() {
        return reservationTheater;
    }

    public String getReservationConfirmationCode() {
        return reservationConfirmationCode;
    }

    public String getReservationTime() {
        return reservationTime;
    }

    public int getReservationId() {
        return reservationId;
    }


//    public ActiveReservationResponse(String reservationMovieTitle, String reservationTheater, String reservationConfirmationCode, String reservationTime, String reservationId) {
//        this.reservationMovieTitle = reservationMovieTitle;
//        this.reservationTheater = reservationTheater;
//        this.reservationConfirmationCode = reservationConfirmationCode;
//        this.reservationTime = reservationTime;
//        this.reservationId = reservationId;
//    }
}
