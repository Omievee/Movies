package com.mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.Constants;
import com.mobile.UserLocationManagerFused;
import com.mobile.adapters.TheaterMoviesAdapter;
import com.mobile.fragments.TheaterFragment;
import com.mobile.helpers.BottomNavigationViewHelper;
import com.mobile.model.Screening;
import com.mobile.model.Theater;
import com.moviepass.R;

import org.parceler.Parcels;

import java.util.ArrayList;

/**
 * Created by anubis on 6/8/17.
 */

public class TheaterActivity extends BaseActivity {

    public static final String EXTRA_CIRCULAR_REVEAL_TRANSITION_NAME = "circular_reveal_transition_name";
    public static final String THEATER = "cinema";
    public static final String RESERVATION = "reservation";
    public static final String SCREENING = "screeningObject";
    public static final String SHOWTIME = "showtime";
    public static final String TOKEN = "token";

    TheaterMoviesAdapter theaterMoviesAdapter;

    ArrayList<Screening> moviesList;
    ArrayList<String> showtimesList;

    protected BottomNavigationView bottomNavigationView;

    Theater theater;
    TextView theaterSelectedName;
    View progress;
    ImageView backArrow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_theater);

        final Toolbar toolbar = findViewById(R.id.CINEMA_TOOLBAR);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Fragment theaterFrag = new TheaterFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.theater_container, theaterFrag).commit();


        bottomNavigationView = findViewById(R.id.CINEAM_NAV);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null){
               extras.get(THEATER);
            }
            theater = Parcels.unwrap(getIntent().getParcelableExtra(THEATER));
        }


        theaterSelectedName = findViewById(R.id.CINEMA_TITLE);
        theaterSelectedName.setText(theater.getName());
        Log.d(Constants.TAG, "onCreate: " + theater.getName());
        moviesList = new ArrayList<>();
        showtimesList = new ArrayList<>();
        backArrow = findViewById(R.id.CINEMA_BACK);
        backArrow.setOnClickListener(v -> onBackPressed());

        UserLocationManagerFused.getLocationInstance(this).startLocationUpdates();

    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("onBackPressed", "onBackPressed");

    }

    /* Bottom Navigation Things */
    int getContentViewId() {
        return R.layout.activity_browse;
    }

    int getNavigationMenuItemId() {
        return R.id.action_theaters;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        bottomNavigationView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int itemId = item.getItemId();
                if (itemId == R.id.action_profile) {
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                } else if (itemId == R.id.action_movies) {
                    startActivity(new Intent(getApplicationContext(), MoviesActivity.class));
                } else if (itemId == R.id.action_theaters) {
                } else if (itemId == R.id.action_settings) {
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                }
                finish();
            }
        }, 300);
        return true;
    }

    private void updateNavigationBarState() {
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    void selectBottomNavigationBarItem(int itemId) {
        Menu menu = bottomNavigationView.getMenu();
        for (int i = 0, size = menu.size(); i < size; i++) {
            MenuItem item = menu.getItem(i);
            boolean shouldBeChecked = item.getItemId() == itemId;
            if (shouldBeChecked) {
                item.setChecked(true);
                break;
            }
        }
    }

}