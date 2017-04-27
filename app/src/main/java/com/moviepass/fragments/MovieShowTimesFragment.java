package com.moviepass.fragments;

import android.app.Fragment;
import android.os.Bundle;

import com.moviepass.model.Movie;

import org.parceler.Parcels;

/**
 * Created by ryan on 4/26/17.
 */

public class MovieShowTimesFragment extends Fragment {

    private static final String MOVIE = "movie";

    public static MovieShowTimesFragment newInstance(Movie movie) {
        MovieShowTimesFragment fragment = new MovieShowTimesFragment();

        Bundle args = new Bundle();
        args.putParcelable(MOVIE, Parcels.wrap(movie));
        fragment.setArguments(args);

        return fragment;
    }

    public MovieShowTimesFragment() {
    }
}
