package com.mobile.model;

import com.mobile.responses.ReservationResponse;

import org.parceler.Parcel;

import javax.annotation.Nullable;

/**
 * Created by anubis on 7/17/17.
 */

@Parcel
public class ScreeningToken {

    ReservationResponse.ETicketConfirmation confirmationCode;
    String qrUrl;
    Reservation reservation;
    Screening screening;
    int seatCol;
    int seatRow;
    String time;
    String zipCodeTicket;
    SeatSelected seatSelected;
    Theater theater;

    public ScreeningToken() {
    }

    public ScreeningToken(Screening screening, String time, Reservation res, Theater theater) {
        this.screening = screening;
        this.time = time;
        this.reservation = res;
        this.theater = theater;
    }

    public ScreeningToken(Screening screening, String time, Reservation res, ReservationResponse.ETicketConfirmation eTicketConfirmation, Theater theater) {
        this.screening = screening;
        this.time = time;
        this.reservation = res;
        this.qrUrl = qrUrl;
        this.confirmationCode = eTicketConfirmation;
        this.theater = theater;
    }

    public ScreeningToken(Screening screening, String time, Reservation res, ReservationResponse.ETicketConfirmation confirmationCode, SeatSelected seatSelected, Theater theater) {
        this.screening = screening;
        this.time = time;
        this.reservation = res;
        this.qrUrl = qrUrl;
        this.confirmationCode = confirmationCode;
        this.seatSelected = seatSelected;
        this.theater = theater;
    }

    public Screening getScreening() {
        return screening;
    }

    public String getTime() {
        return time;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation mReservation) {
        this.reservation = mReservation;
    }


    public void setSeatSelected(SeatSelected seatSelected) {
        this.seatSelected = seatSelected;
    }

    public SeatSelected getSeatSelected() {
        return seatSelected;
    }


    public String getSeatName() {
        if (seatSelected != null) {
            return seatSelected.getSeatName();
        }
        return "";
    }

    public void setZipCodeTicket(String zipCodeTicket) {
        this.zipCodeTicket = zipCodeTicket;
    }

    public String getZipCodeTicket() {
        return zipCodeTicket;
    }

    public ReservationResponse.ETicketConfirmation getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(ReservationResponse.ETicketConfirmation confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    @Nullable public Theater getTheater() {
        return theater;
    }

    public void setQrUrl(String qrUrl) {
        this.qrUrl = qrUrl;
    }

    public String getQrUrl() {
        return qrUrl;
    }
}
