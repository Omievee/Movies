package com.mobile.responses;

import com.mobile.model.Movie;

import java.util.List;

/**
 * Created by o_vicarra on 3/15/18.
 */

public class LocalStorageMovies {
    List<Movie> newReleases;
    List<Movie> featured;
    List<Movie> topBoxOffice;
    List<Movie> nowPlaying;
    List<Movie> comingSoon;


    public List<Movie> getTopBoxOffice() {
        return topBoxOffice;
    }


    public List<Movie> getFeatured() {
        return featured;
    }

    public List<Movie> getNowPlaying() {
        return nowPlaying;
    }

    public List<Movie> getComingSoon() {
        return comingSoon;
    }



    public List<Movie> getNewReleases() {
        return newReleases;
    }

}
