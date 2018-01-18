package com.mobile.fragments;

import android.app.Fragment;
import android.os.Bundle;

import com.mobile.model.Movie;

import org.parceler.Parcels;

/**
 * Created by ryan on 4/26/17.
 */

public class MovieReviewsFragment extends Fragment {

    private static final String MOVIE = "movie";

    public static MovieReviewsFragment newInstance(Movie movie) {
        MovieReviewsFragment fragment = new MovieReviewsFragment();

        Bundle args = new Bundle();
        args.putParcelable(MOVIE, Parcels.wrap(movie));
        fragment.setArguments(args);

        return fragment;
    }

    public MovieReviewsFragment() {

    }
}
