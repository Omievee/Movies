package com.moviepass.requests;

import com.moviepass.model.SelectedSeat;

/**
 * Created by anubis on 6/20/17.
 */

public class TicketInfoRequest {

    PerformanceInfoRequest performanceInfo;
    String theatreNumber;
    SelectedSeatRequest selectedSeatRequest;


    public TicketInfoRequest(PerformanceInfoRequest performanceInfo) {
        this.performanceInfo = performanceInfo;
    }


    public TicketInfoRequest(PerformanceInfoRequest performanceInfo, SelectedSeatRequest selectedSeatRequest) {
        this.theatreNumber = theatreNumber + "";
        this.performanceInfo = performanceInfo;
        this.selectedSeatRequest = selectedSeatRequest;
    }

    public TicketInfoRequest(int theatreNumber, PerformanceInfoRequest performanceInfo) {
        this.theatreNumber = theatreNumber + "";
        this.performanceInfo = performanceInfo;
    }

}