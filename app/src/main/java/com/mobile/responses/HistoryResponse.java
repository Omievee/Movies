package com.mobile.responses;

import com.mobile.model.Movie;

import java.util.List;

/**
 * Created by anubis on 7/31/17.
 */

public class HistoryResponse {

    public List<Movie> movies;

    public List<Movie> getHistory() {
        return movies;
    }

}
