package com.moviepass.model;

import java.util.List;

public class MoviesResponse {

    List<Movie> newReleases;
    List<Movie> topBoxOffice;
    List<Movie> comingSoon;

    public MoviesResponse() {
    }

    public List<Movie> getNewReleases() {
        return newReleases;
    }

    public List<Movie> getTopBoxOffice() {
        return topBoxOffice;
    }

    public List<Movie> getComingSoon() {
        return comingSoon;
    }
}
