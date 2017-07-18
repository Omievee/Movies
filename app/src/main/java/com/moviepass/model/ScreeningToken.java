package com.moviepass.model;

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

    public ScreeningToken() {
    }

    public ScreeningToken(Screening screening, String time, Reservation res) {
        this.screening = screening;
        this.time = time;
        this.reservation = res;
        this.seatName = null;
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

    public void setSelectedSeat(int row, int col, String seatName) {
        this.seatCol = col;
        this.seatRow = row;
        this.seatName = seatName;
    }

    public void setSeatRowAndSeatCol(int row, int col) {
        this.seatRow = row;
        this.seatCol = col;
    }

    public int getSelectedSeatRow() {
        return seatRow;
    }

    public int getSelectedSeatCol() {
        return seatCol;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }

    public String getSeatName() {
        return seatName;
    }

    public void setZipCodeTicket(String zipCodeTicket) {
        this.zipCodeTicket = zipCodeTicket;
    }

    public String getZipCodeTicket() {
        return zipCodeTicket;
    }

    public void setConfirmationCode(String confirmationCode) { this.confirmationCode = confirmationCode; }

    public String getConfirmationCode() {return confirmationCode; }

    public void setQrUrl(String qrUrl) { this.qrUrl = qrUrl; }

    public String getQrUrl() {return qrUrl; }
}
