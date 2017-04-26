package com.moviepass.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.moviepass.R;
import com.moviepass.fragments.MoviesFragment;


/**
 * Created by ryan on 4/26/17.
 */

public class MoviesActivity extends MainActivity {

    MoviesFragment mMoviesFragment = new MoviesFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDoUpdateLocation=false;
        super.onCreate(savedInstanceState);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.container, mMoviesFragment);
        transaction.commit();

    }

}
