package com.mobile.responses;

import com.mobile.model.Movie;

import java.util.List;

/**
 * Created by anubis on 7/31/17.
 */

public class HistoryResponse {

    public Movie getMovie() {
        return movie;
    }

    public List<Movie> movies;
    public Movie movie;

    public List<Movie> getHistory() {
        return movies;
    }

}
