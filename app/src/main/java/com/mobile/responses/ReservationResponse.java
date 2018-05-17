package com.mobile.responses;

import com.mobile.Constants;
import com.mobile.model.Reservation;

import org.parceler.Parcel;

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

    public ETicketConfirmation getE_ticket_confirmation() {return this.e_ticket_confirmation;}

    @Parcel
    public static class ETicketConfirmation {
        private String barCodeUrl;
        private String confirmationCode;

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
    }
}
