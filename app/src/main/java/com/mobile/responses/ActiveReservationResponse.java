package com.mobile.responses;

import com.mobile.model.Reservation;

/**
 * Created by o_vicarra on 12/20/17.
 */

public class ActiveReservationResponse {


    String title;
    String theater;
    String seat;
    String redemption_code;
    String showtime;
    String eTicket;

    public Reservation getReservation() {
        return reservation;
    }

    Reservation reservation;

    public String geteTicket() {
        return eTicket;
    }

    public String getTheater() {
        return theater;
    }

    public String getSeat() {
        return seat;
    }

    public String getRedemption_code() {
        return redemption_code;
    }

    public String getShowtime() {
        return showtime;
    }

    public String getTitle() {
        return title;
    }


//    public ActiveReservationResponse(String reservationMovieTitle, String reservationTheater, String reservationConfirmationCode, String reservationTime, String reservationId) {
//        this.reservationMovieTitle = reservationMovieTitle;
//        this.reservationTheater = reservationTheater;
//        this.reservationConfirmationCode = reservationConfirmationCode;
//        this.reservationTime = reservationTime;
//        this.reservationId = reservationId;
//    }
}
