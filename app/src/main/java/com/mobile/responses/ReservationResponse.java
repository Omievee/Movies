package com.mobile.responses;

import com.mobile.Constants;
import com.mobile.model.Reservation;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcel;

import javax.annotation.Nullable;

/**
 * Created by anubis on 6/20/17.
 */

@Parcel
public class ReservationResponse {

    String status;
    String zip;
    String showtime;
    Reservation reservation;
    ETicketConfirmation e_ticket_confirmation;

    public Reservation getReservation() { return reservation; }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public boolean isOk() {
        return status.matches(Constants.API_RESPONSE_OK);
    }

    public String getZipCode() {
        return zip;
    }

    public String getShowtime() {
        return showtime;
    }

    public ETicketConfirmation getE_ticket_confirmation() {return this.e_ticket_confirmation;}

    @Parcel
    public static class ETicketConfirmation {
        private String barCodeUrl;
        @Nullable
        private String confirmationCode;

        @Nullable
        private String confirmationCodeFormat;

        public String getConfirmationCode() {
            return confirmationCode;
        }

        public void setConfirmationCode(String confirmationCode) {
            this.confirmationCode = confirmationCode;
        }

        public String getBarCodeUrl() {
            return barCodeUrl;
        }

        public void setBarCodeUrl(String barCodeUrl) {
            this.barCodeUrl = barCodeUrl;

        }

        public String getConfirmationCodeFormat() {
            return confirmationCodeFormat;
        }

        public void setConfirmationCodeFormat(String confirmationCodeFormat) {
            this.confirmationCodeFormat = confirmationCodeFormat;
        }
    }
}
