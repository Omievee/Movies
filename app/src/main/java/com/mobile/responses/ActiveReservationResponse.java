package com.mobile.responses;

import com.mobile.model.E_Ticket;
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
    E_Ticket e_ticket;
    String zip;

    public Reservation getReservation() {
        return reservation;
    }

    Reservation reservation;


    public E_Ticket getE_ticket() {
        return e_ticket;
    }


    public String getZip() {
        return zip;
    }


    public String getTheater() {
        return theater;
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
