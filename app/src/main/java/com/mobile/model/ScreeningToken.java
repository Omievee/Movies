package com.mobile.model;

import com.mobile.responses.ETicketConfirmation;

import org.parceler.Parcel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

/**
 * Created by anubis on 7/17/17.
 */

@Parcel
public class ScreeningToken {

    ETicketConfirmation confirmationCode;
    String qrUrl;
    Reservation reservation;
    Screening screening;
    int seatCol;
    int seatRow;
    String time;
    String zipCodeTicket;
    List<SeatSelected> seatSelected;
    Theater theater;

    public ScreeningToken() {
    }

    public ScreeningToken(Screening screening, String time, Reservation res, Theater theater) {
        this.screening = screening;
        this.time = time;
        this.reservation = res;
        this.theater = theater;
    }

    public ScreeningToken(Screening screening, String time, Reservation res, ETicketConfirmation eTicketConfirmation, Theater theater) {
        this.screening = screening;
        this.time = time;
        this.reservation = res;
        this.confirmationCode = eTicketConfirmation;
        this.theater = theater;
    }

    public ScreeningToken(Screening screening, String time, Reservation res, ETicketConfirmation confirmationCode, List<SeatSelected> seatSelected, Theater theater) {
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

    @Nullable
    public Date getTimeAsDate() {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).parse(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setReservation(Reservation mReservation) {
        this.reservation = mReservation;
    }


    @Nullable public List<SeatSelected> getSeatSelected() {
        return seatSelected;
    }

    public ETicketConfirmation getConfirmationCode() {
        return confirmationCode;
    }

    @Nullable
    public Theater getTheater() {
        return theater;
    }

}
