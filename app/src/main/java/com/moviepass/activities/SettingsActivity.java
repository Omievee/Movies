package com.moviepass.activities;

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
import android.widget.Toast;

import com.moviepass.R;
import com.moviepass.fragments.SettingsFragment;
import com.moviepass.helpers.BottomNavigationViewHelper;

/**
 * Created by anubis on 6/9/17.
 */

public class SettingsActivity extends BaseActivity {

    SettingsFragment settingsFragment = new SettingsFragment();
    protected BottomNavigationView bottomNavigationView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();

        // Enable the Up button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Settings");

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.container, settingsFragment);
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
            startActivity(new Intent(SettingsActivity.this, MoviesActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    int getContentViewId() {
        return R.layout.activity_settings;
    }

    int getNavigationMenuItemId() {
        return R.id.action_settings;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        bottomNavigationView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int itemId = item.getItemId();
                if (itemId == R.id.action_profile) {
                    startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
//                } else if (itemId == R.id.action_reservations) {
//                    startActivity(new Intent(SettingsActivity.this, ReservationsActivity.class));
                } else if (itemId == R.id.action_movies) {
                    startActivity(new Intent(SettingsActivity.this, MoviesActivity.class));
                } else if (itemId == R.id.action_theaters) {
                    startActivity(new Intent(SettingsActivity.this, TheatersActivity.class));
                } else if (itemId == R.id.action_settings) {
                    startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
                }
            }
        }, 300);
        return true;
    }

    private void updateNavigationBarState(){
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