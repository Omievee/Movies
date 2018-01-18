package com.mobile.requests;

/**
 * Created by anubis on 6/20/17.
 */

public class CheckInRequest {

    String screeningTime;
    double latitude;
    double longitude;
    String providerName;
    TicketInfoRequest ticketInfo;

    public CheckInRequest(TicketInfoRequest ticketInfo, String providerName, double latitude, double longitude) {
        this.providerName = providerName;
        this.ticketInfo = ticketInfo;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}