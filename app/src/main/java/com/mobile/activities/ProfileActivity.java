package com.mobile.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.mobile.fragments.ProfileFragment;
import com.mobile.helpers.BottomNavigationViewHelper;
import com.moviepass.R;

/**
 * Created by anubis on 7/23/17.
 */

public class ProfileActivity extends BaseActivity {
    public static final String TAG = "Found it";

//    ProfileAccountInformationFragment profileFragment = new ProfileAccountInformationFragment();
    ProfileFragment profileFragment = new ProfileFragment();
    protected BottomNavigationView bottomNavigationView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();



        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.profile_container, profileFragment);
        transaction.commit();

        bottomNavigationView = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            FragmentManager fm = getFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
            } else {
                startActivity(new Intent(ProfileActivity.this, MoviesActivity.class));
            }
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    int getContentViewId() {
        return R.layout.activity_profile;
    }

    int getNavigationMenuItemId() {
        return R.id.action_profile;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        bottomNavigationView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int itemId = item.getItemId();
                if (itemId == R.id.action_profile) {
                } else if (itemId == R.id.action_movies) {
                    startActivity(new Intent(getApplicationContext(), MoviesActivity.class));
                } else if (itemId == R.id.action_theaters) {
                    startActivity(new Intent(getApplicationContext(), TheatersActivity.class));
                } else if (itemId == R.id.action_settings) {
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                }
            }
        }, 0);
        return true;
    }

    //} else if (itemId == R.id.action_reservations) {
//    startActivity(new Intent(getApplicationContext(), ReservationsActivity.class));
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
