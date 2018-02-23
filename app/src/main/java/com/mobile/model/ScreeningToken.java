package com.mobile.model;

import org.parceler.Parcel;

/**
 * Created by anubis on 7/17/17.
 */

@Parcel
public class ScreeningToken {

    String confirmationCode;
    String qrUrl;
    Reservation reservation;
    Screening screening;
    int seatCol;
    String seatName;
    int seatRow;
    String time;
    String zipCodeTicket;
    SeatSelected seatSelected;

    public ScreeningToken() {
    }

    public ScreeningToken(Screening screening, String time, Reservation res) {
        this.screening = screening;
        this.time = time;
        this.reservation = res;
        this.seatName = null;
    }

    public ScreeningToken(Screening screening, String time, Reservation res, String qrUrl, String confirmationCode) {
        this.screening = screening;
        this.time = time;
        this.reservation = res;
        this.qrUrl = qrUrl;
        this.confirmationCode = confirmationCode;
    }

    public ScreeningToken(Screening screening, String time, Reservation res, String qrUrl, String confirmationCode, SeatSelected seatSelected) {
        this.screening = screening;
        this.time = time;
        this.reservation = res;
        this.qrUrl = qrUrl;
        this.confirmationCode = confirmationCode;
        this.seatSelected = seatSelected;
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
        return seatSelected.getSeatName();
    }

    public void setZipCodeTicket(String zipCodeTicket) {
        this.zipCodeTicket = zipCodeTicket;
    }

    public String getZipCodeTicket() {
        return zipCodeTicket;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setQrUrl(String qrUrl) {
        this.qrUrl = qrUrl;
    }

    public String getQrUrl() {
        return qrUrl;
    }
}
