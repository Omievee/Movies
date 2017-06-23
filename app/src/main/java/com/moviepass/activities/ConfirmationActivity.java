package com.moviepass.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.helpers.BottomNavigationViewHelper;
import com.moviepass.model.Reservation;
import com.moviepass.model.Screening;
import com.moviepass.network.RestCallback;
import com.moviepass.network.RestClient;
import com.moviepass.network.RestError;
import com.moviepass.requests.ChangedMindRequest;
import com.moviepass.responses.ChangedMindResponse;

import org.parceler.Parcels;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by anubis on 6/20/17.
 */

public class ConfirmationActivity extends BaseActivity {

    public static final String RESERVATION = "reservation";
    public static final String SCREENING = "screening";

    Reservation mReservation;
    Screening mScreening;
    FloatingActionMenu mFab;
    View mProgress;

    TextView mMovieTitle;
    TextView mTheater;

    protected BottomNavigationView bottomNavigationView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        bottomNavigationView = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        mFab = findViewById(R.id.menuActions);
        mProgress = findViewById(R.id.progress);

        Bundle extras = getIntent().getExtras();
        mScreening = Parcels.unwrap(getIntent().getParcelableExtra(SCREENING));
        mReservation = Parcels.unwrap(getIntent().getParcelableExtra(RESERVATION));

        mMovieTitle = findViewById(R.id.movie_title);
        mTheater = findViewById(R.id.theater);

        mMovieTitle.setText(mScreening.getTitle());
        mTheater.setText(mScreening.getTheaterName());

        FloatingActionButton buttonChangeReservation = new FloatingActionButton(this);
        buttonChangeReservation.setLabelText(getText(R.string.activity_confirmation_change_reservation).toString());
        buttonChangeReservation.setImageResource(R.drawable.icon_reset);
        buttonChangeReservation.setButtonSize(FloatingActionButton.SIZE_MINI);
        buttonChangeReservation.setColorNormalResId(R.color.red);
        buttonChangeReservation.setColorPressedResId(R.color.red_dark);
        mFab.addMenuButton(buttonChangeReservation);

        buttonChangeReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.setVisibility(View.VISIBLE);
                ChangedMindRequest request = new ChangedMindRequest(mReservation.getId());

                RestClient.getAuthenticated().changedMind(request).enqueue(new RestCallback<ChangedMindResponse>() {
                    @Override
                    public void onResponse(Call<ChangedMindResponse> call, Response<ChangedMindResponse> response) {
                        ChangedMindResponse responseBody = response.body();

                        if (responseBody != null){
//                            UserPreferences.clearSuccessfulReservationCount(0);
                            Toast.makeText(ConfirmationActivity.this, responseBody.getMessage(), Toast.LENGTH_LONG).show();
                            finish();
                        }

                    }

                    @Override
                    public void failure(RestError restError) {
                            mProgress.setVisibility(View.GONE);
                            mFab.setEnabled(true);
                            Toast.makeText(ConfirmationActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();

                    }

                });
            }
        });
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
