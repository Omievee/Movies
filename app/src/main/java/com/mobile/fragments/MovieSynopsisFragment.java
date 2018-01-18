package com.mobile.fragments;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by ryan on 4/26/17.
 */

public class MovieSynopsisFragment extends Fragment {

    private static final String ARG_SYNOPSIS = "synopsis";

    public static MovieSynopsisFragment newInstance(String synopsis) {
        MovieSynopsisFragment fragment = new MovieSynopsisFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SYNOPSIS, synopsis);
        fragment.setArguments(args);

        return fragment;
    }

    public MovieSynopsisFragment() {
    }
}
