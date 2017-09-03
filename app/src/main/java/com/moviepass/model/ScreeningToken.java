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
    SelectedSeat selectedSeat;

    public ScreeningToken() {
    }

    public ScreeningToken(Screening screening, String time, Reservation res) {
        this.screening = screening;
        this.time = time;
        this.reservation = res;
        this.seatName = null;
    }


    public ScreeningToken(Screening screening, String time, Reservation res, String qrUrl, String confirmationCode, SelectedSeat selectedSeat) {
        this.screening = screening;
        this.time = time;
        this.reservation = res;
        this.qrUrl = qrUrl;
        this.confirmationCode = confirmationCode;
        this.selectedSeat = selectedSeat;
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

    public void setSelectedSeat(SelectedSeat selectedSeat) { this.selectedSeat = selectedSeat; }

    public SelectedSeat getSelectedSeat() { return selectedSeat; }

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
