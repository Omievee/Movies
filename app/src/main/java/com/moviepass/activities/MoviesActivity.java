package com.moviepass.activities;

import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.moviepass.R;
import com.moviepass.fragments.MoviesFragment;


/**
 * Created by ryan on 4/26/17.
 */

public class MoviesActivity extends BaseActivity {

    MoviesFragment mMoviesFragment = new MoviesFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    int getContentViewId() {
        return R.layout.activity_movie;
    }

    int getNavigationMenuItemId() {
        return R.id.action_browse;
    }

}
