package com.moviepass.activities;

import android.animation.Animator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;

import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.fragments.MoviesFragment;
import com.moviepass.fragments.TheatersFragment;
import com.moviepass.helpers.BottomNavigationViewHelper;
import com.moviepass.model.Theater;

import org.parceler.Parcels;

import butterknife.BindView;

/**
 * Created by anubis on 8/4/17.
 */

public class TheatersActivity extends BaseActivity implements TheatersFragment.OnTheaterSelect {

    @BindView(R.id.red)
    View redView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theaters);

        /* TODO: Set up active reservation later
        viewPager = findViewById(R.id.pager);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        setupViewPager(viewPager); */

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();

        // Enable the Up button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Theaters");

        redView = findViewById(R.id.red);
        redView.setVisibility(View.INVISIBLE);

        Fragment theatersFragment = new TheatersFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, theatersFragment).commit();

        bottomNavigationView = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        redView.setVisibility(View.INVISIBLE);
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onTheaterSelect(int pos, Theater theater, int cx, int cy) {
        final Theater finalTheater = theater;

        Intent intent = new Intent(TheatersActivity.this, TheaterActivity.class);
        intent.putExtra(TheaterActivity.THEATER, Parcels.wrap(finalTheater));

        startActivity(intent);


        //TODO Bring back animations when polished.. Simpler ones?
//        if (Build.VERSION.SDK_INT >= 21) {
//            redView.bringToFront();
//
//            int cxFinal = redView.getWidth() / 2;
//            int cyFinal = redView.getHeight() / 2;
//
//            float finalRadius = (float) Math.hypot(cxFinal, cyFinal);
//
//           Animator anim =
//                   ViewAnimationUtils.createCircularReveal(redView, cx, cy, 0, finalRadius);
//
//            redView.setVisibility(View.VISIBLE);
//            anim.start();
//            anim.addListener(new Animator.AnimatorListener() {
//                @Override
//                public void onAnimationStart(Animator animator) {
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animator) {
//                    Intent intent = new Intent(TheatersActivity.this, TheaterActivity.class);
//                    intent.putExtra(TheaterActivity.THEATER, Parcels.wrap(finalTheater));
//
//                    startActivity(intent);
//                }
//
//                @Override
//                public void onAnimationCancel(Animator animator) {
//                }
//
//                @Override
//                public void onAnimationRepeat(Animator animator) {
//                }
//            });
//        }
    }

    int getContentViewId() {
        return R.layout.activity_theaters;
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
                    if (UserPreferences.getUserId() == 0) {
                        Intent intent = new Intent(TheatersActivity.this, LogInActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(TheatersActivity.this, ProfileActivity.class);
                        startActivity(intent);
                    }
                } else if (itemId == R.id.action_reservations) {
                    startActivity(new Intent(TheatersActivity.this, ReservationsActivity.class));
                } else if (itemId == R.id.action_movies) {
                    startActivity(new Intent(TheatersActivity.this, MoviesActivity.class));
                } else if (itemId == R.id.action_theaters) {
                } else if (itemId == R.id.action_settings) {
                    startActivity(new Intent(TheatersActivity.this, SettingsActivity.class));
                }
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
