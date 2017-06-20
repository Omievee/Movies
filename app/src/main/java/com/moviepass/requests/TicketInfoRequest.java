package com.moviepass.requests;

import com.moviepass.model.SelectedSeat;

/**
 * Created by anubis on 6/20/17.
 */

public class TicketInfoRequest {

    PerformanceInfoRequest performanceInfo;
    String theatreNumber;
    SelectedSeat selectedSeat;


    public TicketInfoRequest(PerformanceInfoRequest performanceInfo) {
        this.performanceInfo = performanceInfo;
    }


    public TicketInfoRequest(PerformanceInfoRequest performanceInfo, SelectedSeat selectedSeat) {
        this.theatreNumber = theatreNumber + "";
        this.performanceInfo = performanceInfo;
        this.selectedSeat = selectedSeat;
    }

    public TicketInfoRequest(int theatreNumber, PerformanceInfoRequest performanceInfo) {
        this.theatreNumber = theatreNumber + "";
        this.performanceInfo = performanceInfo;
    }

}