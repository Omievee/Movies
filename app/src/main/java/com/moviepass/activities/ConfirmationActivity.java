package com.moviepass.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.moviepass.R;
import com.moviepass.helpers.BottomNavigationViewHelper;
import com.moviepass.model.Reservation;
import com.moviepass.model.Screening;
import com.moviepass.model.ScreeningToken;
import com.moviepass.network.RestCallback;
import com.moviepass.network.RestClient;
import com.moviepass.network.RestError;
import com.moviepass.requests.ChangedMindRequest;
import com.moviepass.responses.ChangedMindResponse;

import org.json.JSONObject;
import org.parceler.Parcels;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by anubis on 6/20/17.
 */

public class ConfirmationActivity extends BaseActivity {

    public static final String RESERVATION = "reservation";
    public static final String SCREENING = "mScreening";
    public static final String TOKEN = "token";

    Reservation reservation;
    Screening screening;
    ScreeningToken screeningToken;
    FloatingActionMenu fabCancelReservation;
    View progress;

    ImageView poster;
    ImageView mask;
    TextView confirmedMovieTitle;
    TextView theater;
    TextView address;
    TextView cityThings;
    TextView confirmedShowTime;
    TextView date;
    TextView auditorium;
    TextView seat;
    TextView confirmedMessage;
    ImageView qrCode;
    TextView cancelReservation;
    TextView confirmationCode;
    RelativeLayout ticketTop;
    RelativeLayout ticketBottom;

    protected BottomNavigationView bottomNavigationView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_confirmation);

        bottomNavigationView = findViewById(R.id.CONFIRMED_BOTTOMNAV);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        progress = findViewById(R.id.confirm_progress);

        Bundle extras = getIntent().getExtras();

        screeningToken = Parcels.unwrap(getIntent().getParcelableExtra(TOKEN));
        screening = screeningToken.getScreening();
        reservation = screeningToken.getReservation();
        String screeningTime = screeningToken.getTime();

//        poster = findViewById(R.id.poster);
//        mask = findViewById(R.id.mask);
        confirmedMovieTitle = findViewById(R.id.CONFIRMED_MOVIE_TITLE);
        theater = findViewById(R.id.CONFIRMED_THEATER);
        confirmationCode = findViewById(R.id.CONFIRMED_CONFIRMATION_CODE);
//        address = findViewById(R.id.address);
//        cityThings = findViewById(R.id.city_things);
        confirmedShowTime = findViewById(R.id.CONFIRMED_SHOWTIME);
//        date = findViewById(R.id.date);
//        auditorium = findViewById(R.id.auditorium);
//        seat = findViewById(R.id.seat);
//        ticketTop = findViewById(R.id.ticket_top);
//        ticketBottom = findViewById(R.id.ticket_bottom);
        confirmedMessage = findViewById(R.id.CONFIRMED_READY_MESSAGE);
//        qrCode = findViewById(R.id.qr_code);

        confirmedMovieTitle.setText(screening.getTitle());
        theater.setText(screening.getTheaterName());
//        address.setText(screening.getTheaterAddress());
//        cityThings.setVisibility(View.GONE);
        confirmedShowTime.setText(screeningTime);
        cancelReservation = findViewById(R.id.CONFIRMED_CANCEL);
//        try {
//            SimpleDateFormat inputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//            Log.d("screeningGetDate", screening.getDate());
//            Date createdAt = inputDate.parse(screening.getDate());
//
//            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
//            String finalDate = sdf.format(createdAt);
//
//            Log.d("finalDate", finalDate);
//
//            date.setText(finalDate);
//
//        } catch (Exception e) {
//            Log.d("exception", e.toString());
//        }

        /* TODO : Add Auditorum logic */
//        auditorium.setVisibility(View.GONE);

//        if(screeningToken.getSeatName() != null) {
//            seat.setText(screeningToken.getSeatName());
//
//        }

//        String imgUrl = screening.getImageUrl();
//        Log.d("imgUrl", imgUrl);
//
//        Picasso.Builder builder = new Picasso.Builder(this);
//        builder.build()
//                .load(imgUrl)
//                .error(R.drawable.confirmation_ticket_top_red)
//                .centerCrop()
//                .fit()
//                .into(poster);

        if (screeningToken.getConfirmationCode() != null) {
            String code = screeningToken.getConfirmationCode();

            if (screeningToken.getSelectedSeat() != null) {
                String seatName = screeningToken.getSelectedSeat().getSeatName();

                Log.d("seatName", seatName);
                String fullConfirmationCodeInstructionsWithSeat = getString(R.string.activity_confirmation_pick_up_instructions) + " " +
                        getString(R.string.activity_confirmation_confirmation_text) + ". " + confirmationCode + " " +
                        getString(R.string.activity_confirmation_seat_selected) + " " + seatName;

                confirmedMessage.setText(fullConfirmationCodeInstructionsWithSeat);
            } else {

                confirmationCode.setText(code);
            }
        } else if (screeningToken.getQrUrl() != null && !screeningToken.getQrUrl().matches("http://www.moviepass.com/images/amc/qrcode.png")) {

            //TODO QR CODE:
//            String qrUrl = screeningToken.getQrUrl();
//
//            Picasso.Builder qrBuilder = new Picasso.Builder(this);
//            qrBuilder.build()
//                    .load(qrUrl)
//                    .centerCrop()
//                    .fit()
//                    .into(qrCode);
        }
//
//        FloatingActionButton buttonChangeReservation = new FloatingActionButton(this);
//        buttonChangeReservation.setLabelText(getText(R.string.activity_confirmation_change_reservation).toString());
//        buttonChangeReservation.setImageResource(R.drawable.icon_reset);
//        buttonChangeReservation.setButtonSize(FloatingActionButton.SIZE_MINI);
//        buttonChangeReservation.setColorNormalResId(R.color.red);
//        buttonChangeReservation.setColorPressedResId(R.color.red_dark);
//        fabCancelReservation.addMenuButton(buttonChangeReservation);

        if (screeningToken.getConfirmationCode() != null) {
            cancelReservation.setVisibility(View.GONE);
        }

        cancelReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                Log.d("jObjError", "jObjError: " + jObjError.getString("message"));

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
                                Log.d("jObjError", "jObjError: " + jObjError.getString("message"));

                                Toast.makeText(ConfirmationActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                            }
                        }
                    }

                    @Override
                    public void failure(RestError restError) {
                        progress.setVisibility(View.GONE);
                        fabCancelReservation.setEnabled(true);
                        Toast.makeText(ConfirmationActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
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
