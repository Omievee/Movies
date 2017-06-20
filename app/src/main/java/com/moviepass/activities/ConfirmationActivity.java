package com.moviepass.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.moviepass.R;
import com.moviepass.helpers.BottomNavigationViewHelper;
import com.moviepass.model.Screening;

import org.parceler.Parcels;

/**
 * Created by anubis on 6/20/17.
 */

public class ConfirmationActivity extends BaseActivity {

    public static final String SCREENING = "screening";

    Screening mScreening;

    TextView mMovieTitle;
    TextView mTheater;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        bottomNavigationView = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        Bundle extras = getIntent().getExtras();
        mScreening = Parcels.unwrap(getIntent().getParcelableExtra(SCREENING));

        mMovieTitle = findViewById(R.id.movie_title);
        mTheater = findViewById(R.id.theater);

        mMovieTitle.setText(mScreening.getTitle());
        mTheater.setText(mScreening.getTheaterName());

    }



    /* Bottom Navigation View */

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
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                } else if (itemId == R.id.action_reservations) {
                    Toast.makeText(ConfirmationActivity.this, "E-Ticket Activity", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), ETicketsActivity.class));
                } else if (itemId == R.id.action_browse) {
                    startActivity(new Intent(getApplicationContext(), BrowseActivity.class));
                } else if (itemId == R.id.action_notifications) {
                    Toast.makeText(ConfirmationActivity.this, "Notification Activity", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), NotificationsActivity.class));
                } else if (itemId == R.id.action_settings) {
                    Toast.makeText(ConfirmationActivity.this, "Settings Activity", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                }
                finish();
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
