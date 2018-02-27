package com.mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.fragments.TicketVerificationDialog;
import com.mobile.helpers.BottomNavigationViewHelper;
import com.mobile.model.Reservation;
import com.mobile.model.Screening;
import com.mobile.model.ScreeningToken;
import com.mobile.network.RestCallback;
import com.mobile.network.RestClient;
import com.mobile.network.RestError;
import com.mobile.requests.ChangedMindRequest;
import com.mobile.responses.ChangedMindResponse;
import com.mobile.responses.UserInfoResponse;
import com.moviepass.R;

import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
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
    String ZIP;
    TextView noCurrentRes, pendingTitle, pendingLocal, pendingTime, pendingSeat, confirmCode, zip;
    Button cancelButton;
    RelativeLayout pendingData, StandardTicket, ETicket;


    protected BottomNavigationView bottomNavigationView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_confirmation);

        bottomNavigationView = findViewById(R.id.CONFIRMED_BOTTOMNAV);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        progress = findViewById(R.id.confirm_progress);
        screeningToken = Parcels.unwrap(getIntent().getParcelableExtra(TOKEN));
        screening = screeningToken.getScreening();
        reservation = screeningToken.getReservation();
        String screeningTime = screeningToken.getTime();


        noCurrentRes = findViewById(R.id.NO_Current_Res);
        pendingTitle = findViewById(R.id.PendingRes_Title);
        pendingLocal = findViewById(R.id.PendingRes_Location);
        pendingTime = findViewById(R.id.PendingRes_Time);
        pendingSeat = findViewById(R.id.PendingRes_Seat);
        StandardTicket = findViewById(R.id.STANDARD_TICKET);
        ETicket = findViewById(R.id.E_TICKET);
        confirmCode = findViewById(R.id.ConfirmCode);
        cancelButton = findViewById(R.id.PEndingRes_Cancel);
        zip = findViewById(R.id.PendingZip);
        pendingData = findViewById(R.id.PENDING_DATA);


        pendingTitle.setText(screeningToken.getScreening().getTitle());
        pendingLocal.setText(screeningToken.getScreening().getTheaterName());
        pendingTime.setText(screeningTime);
        userData();

        if (screeningToken.getConfirmationCode() != null) {
            ETicket.setVisibility(View.VISIBLE);
            String code = screeningToken.getConfirmationCode();
            confirmCode.setText(code);
            if (screeningToken.getSeatName() != null) {
                pendingSeat.setVisibility(View.VISIBLE);
                pendingSeat.setText("Seat: " + screeningToken.getSeatName());
            }
        } else {
            StandardTicket.setVisibility(View.VISIBLE);
            if (!UserPreferences.getIsVerificationRequired()) {
                Bundle bundle = new Bundle();
                TicketVerificationDialog dialog = new TicketVerificationDialog();
                dialog.setArguments(bundle);
                FragmentManager fm = getSupportFragmentManager();
                dialog.setCancelable(false);
                dialog.show(fm, "fr_ticketverification_banner");
            }


        }


        cancelButton.setOnClickListener(v -> {
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

    public void userData() {
        int userId = UserPreferences.getUserId();
        RestClient.getAuthenticated().getUserData(userId).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                userInfoResponse = response.body();
                if (userInfoResponse != null) {
                    String address = userInfoResponse.getShippingAddressLine2();
                    List<String> addressList = Arrays.asList(address.split(",", -1));

                    for (int i = 0; i < addressList.size(); i++) {
                        ZIP = addressList.get(2);
                        zip.setText(ZIP);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
            }
        });
    }
}
