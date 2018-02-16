package com.mobile.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mobile.helpers.BottomNavigationViewHelper;
import com.mobile.model.Reservation;
import com.mobile.model.Screening;
import com.mobile.model.ScreeningToken;
import com.mobile.network.RestCallback;
import com.mobile.network.RestClient;
import com.mobile.network.RestError;
import com.mobile.requests.ChangedMindRequest;
import com.mobile.responses.ChangedMindResponse;
import com.moviepass.R;

import org.json.JSONObject;
import org.parceler.Parcels;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by anubis on 6/20/17.
 */

public class ConfirmationActivity extends BaseActivity {
    public static final String TAG = " found it ";
    public static final String RESERVATION = "reservation";
    public static final String SCREENING = "screeningObject";
    public static final String TOKEN = "token";

    Reservation reservation;
    Screening screening;
    ScreeningToken screeningToken;
    View progress;

    ImageView cancelX;
    TextView confirmedMovieTitle;
    TextView theater;
    TextView confirmedShowTime;
    TextView confirmedMessage;
    ImageView qrCode;
    TextView cancelReservation;
    TextView confirmationCode;
    SimpleDraweeView moviepassCC_QR;
    ImageView loadCardLogo;
    TextView confirmedZipText, thirtyMins;
    protected BottomNavigationView bottomNavigationView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_confirmation);

        bottomNavigationView = findViewById(R.id.CONFIRMED_BOTTOMNAV);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        thirtyMins = findViewById(R.id.thirtyMins);

        progress = findViewById(R.id.confirm_progress);
        screeningToken = Parcels.unwrap(getIntent().getParcelableExtra(TOKEN));
        screening = screeningToken.getScreening();
        reservation = screeningToken.getReservation();
        String screeningTime = screeningToken.getTime();


        confirmedZipText = findViewById(R.id.CONFIRMED_ZIP_TEXT);
        confirmedMovieTitle = findViewById(R.id.CONFIRMED_MOVIE_TITLE);
        theater = findViewById(R.id.CONFIRMED_THEATER);
        confirmationCode = findViewById(R.id.CONFIRMED_CONFIRMATION_CODE);
        moviepassCC_QR = findViewById(R.id.CONFIRMED_MASTERCARD);
        moviepassCC_QR.setImageResource(R.drawable.mpmastercard2);
        confirmedShowTime = findViewById(R.id.CONFIRMED_SHOWTIME);
        confirmedMessage = findViewById(R.id.CONFIRMED_READY_MESSAGE);
        loadCardLogo = findViewById(R.id.CONFIRMED_LOADED_LOGO);
        confirmedMovieTitle.setText(screening.getTitle());
        theater.setText(screening.getTheaterName());
        confirmedShowTime.setText(screeningTime);
        cancelReservation = findViewById(R.id.CONFIRMED_CANCEL);
        cancelX = findViewById(R.id.CONFIRMED_X_BUTTON);

        Log.d(TAG, "onCreate: " + screeningToken.getZipCodeTicket());

        userData();

        if (screeningToken.getConfirmationCode() != null) {
            confirmedZipText.setVisibility(View.VISIBLE);
            confirmationCode.setVisibility(View.VISIBLE);
            String code = screeningToken.getConfirmationCode();
            thirtyMins.setVisibility(View.GONE);
            String zip = screeningToken.getZipCodeTicket();
            Log.d(TAG, "onCreate: " + zip);
            confirmationCode.setText("If asked, ");
            confirmedZipText.setText("Here is your redemption code:");

            Log.d(TAG, "onCreate:  " + myZip);
            if (code == null) {
                confirmationCode.setText(myZip);
            } else {
                cancelReservation.setVisibility(View.GONE);
                confirmationCode.setText(code);
            }
            if (screeningToken.getQrUrl() != null && screeningToken.getQrUrl().matches("http://www.moviepass.com/images/amc/qrcode.png")) {
                Uri qrUrl = Uri.parse(screeningToken.getQrUrl());
                if (qrUrl != null) {
                    cancelReservation.setVisibility(View.GONE);
                    //TODO :  QR?

//                    loadCardLogo.setVisibility(View.INVISIBLE);
//
//
//                    ImageRequest request = ImageRequestBuilder.newBuilderWithSource(qrUrl)
//                            .setProgressiveRenderingEnabled(true)zx
//                            .setSource(qrUrl)
//                            .build();
//
//                    DraweeController controller = Fresco.newDraweeControllerBuilder()
//                            .setImageRequest(request).build();
//
//                    moviepassCC_QR.setImageURI(qrUrl);
//                    moviepassCC_QR.setController(controller);

                }
            }

        }
        cancelReservation.setOnClickListener(v -> {
            progress.setVisibility(View.VISIBLE);
            ChangedMindRequest request = new ChangedMindRequest(reservation.getId());
            RestClient.getAuthenticated().changedMind(request).enqueue(new RestCallback<ChangedMindResponse>() {
                @Override
                public void onResponse(Call<ChangedMindResponse> call, Response<ChangedMindResponse> response) {
                    ChangedMindResponse responseBody = response.body();
                    progress.setVisibility(View.GONE);

                    if (responseBody != null && responseBody.getMessage().matches("Failed to cancel reservation: You have already purchased your ticket.")) {
                        try {
                            JSONObject jObjError = new JSONObject(response.errorBody().string());

                            Toast.makeText(ConfirmationActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG);
                        } catch (Exception e) {
                        }
                    } else if (responseBody != null && responseBody.getMessage().matches("Failed to cancel reservation: You do not have a pending reservation.")) {
                        finish();
                    } else if (responseBody != null && response.isSuccessful()) {
                        Toast.makeText(ConfirmationActivity.this, responseBody.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        try {
                            JSONObject jObjError = new JSONObject(response.errorBody().string());

                            Toast.makeText(ConfirmationActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                        }
                    }
                }

                @Override
                public void failure(RestError restError) {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(ConfirmationActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
        cancelX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backToMain = new Intent(getApplicationContext(), MoviesActivity.class);
                startActivity(backToMain);
            }
        });
    }

    /* Bottom Navigation View */

    int getContentViewId() {
        return R.layout.activity_movies;
    }

    int getNavigationMenuItemId() {
        return R.id.action_movies;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        bottomNavigationView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int itemId = item.getItemId();
                if (itemId == R.id.action_profile) {
                    startActivity(new Intent(ConfirmationActivity.this, ProfileActivity.class));
                } else if (itemId == R.id.action_movies) {
                } else if (itemId == R.id.action_theaters) {
                } else if (itemId == R.id.action_settings) {
                    startActivity(new Intent(ConfirmationActivity.this, SettingsActivity.class));
                }
                finish();
            }
        }, 300);
        return true;

//        else if (itemId == R.id.action_reservations) {
//            startActivity(new Intent(ConfirmationActivity.this, ReservationsActivity.class));
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
