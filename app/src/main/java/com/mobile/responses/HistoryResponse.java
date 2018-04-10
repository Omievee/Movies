package com.mobile.responses;

import com.mobile.model.Movie;

import java.util.List;

/**
 * Created by anubis on 7/31/17.
 */

public class HistoryResponse {


    public List<Movie> reservations;
    String rating;


    public HistoryResponse(String rating) {
        this.rating = rating;
    }
    public List<Movie> getReservations() {
        return reservations;
    }


}
