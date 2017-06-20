package com.moviepass.responses;

import com.moviepass.Constants;
import com.moviepass.model.Reservation;

/**
 * Created by anubis on 6/20/17.
 */

public class ReservationResponse {

    String status;
    String zip;
    String showtime;
    Reservation reservation;
    eTicketConfirmation e_ticket_confirmation;

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

    public eTicketConfirmation getE_ticket_confirmation() {return this.e_ticket_confirmation;}

    public class eTicketConfirmation {
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
