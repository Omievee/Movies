package com.mobile.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mobile.UserPreferences;
import com.mobile.helpers.LogUtils;
import com.mobile.home.HomeActivity;
import com.mobile.model.Availability;
import com.mobile.model.Reservation;
import com.mobile.model.Screening;
import com.mobile.model.ScreeningToken;
import com.mobile.model.SeatSelected;
import com.mobile.model.Theater;
import com.mobile.network.RestCallback;
import com.mobile.network.RestClient;
import com.mobile.network.RestError;
import com.mobile.requests.SelectedSeat;
import com.mobile.requests.TicketInfoRequest;
import com.mobile.reservation.ReservationActivity;
import com.mobile.responses.ReservationResponse;
import com.moviepass.R;

import org.json.JSONObject;
import org.parceler.Parcels;

import retrofit2.Call;
import retrofit2.Response;

public class EticketConfirmation extends BaseActivity {

    TextView etixTitle, etixTheater, etixShowtime, etixSeat;
    SimpleDraweeView etixPoster;
    ImageView etixOnBack;
    Screening screeningObject;
    SeatSelected seatObject;
    Button etixConfirm;
    String selectedShowTime;
    View progressWheel;
    RelativeLayout relSeat;
    TicketInfoRequest ticketRequest;
    Theater theater;
    public static final String SEAT = "seat";
    public static final String TAG = "FOUND IT";

    public static final String MOVIE = "movie";
    public static final String SCREENING = "screening";
    public static final String SHOWTIME = "showtime";
    public static final String THEATER = "theater";
    public static final String TOKEN = "token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_eticket_confirmation);

        etixConfirm = findViewById(R.id.ETIX_GET);
        etixTitle = findViewById(R.id.ETIX_MOVIE_TITLE);
        etixShowtime = findViewById(R.id.ETIX_SHOWTIME);
        etixPoster = findViewById(R.id.ETIX_MOVIEPOSTER);
        etixTheater = findViewById(R.id.ETIX_THEATER);
        etixSeat = findViewById(R.id.ETIX_SEAT);
        etixOnBack = findViewById(R.id.Etix_ONBACK);
        relSeat = findViewById(R.id.relSeat);

        //set details for confirmation page..

        Intent intent = getIntent();
        screeningObject = intent.getParcelableExtra(SCREENING);
        selectedShowTime = getIntent().getStringExtra(SHOWTIME);
        seatObject = Parcels.unwrap(getIntent().getParcelableExtra(SEAT));
        theater = Parcels.unwrap(getIntent().getParcelableExtra(THEATER));

        etixTitle.setText(screeningObject.getTitle());
        etixShowtime.setText(selectedShowTime);
        etixTheater.setText(screeningObject.getTheaterName());

        if (seatObject != null) {
            etixSeat.setText(seatObject.getSeatName());
            relSeat.setVisibility(View.VISIBLE);
        }

        progressWheel = findViewById(R.id.etixprogress);
        Uri uri = Uri.parse(screeningObject.getImageUrl());
        etixPoster.setImageURI(uri);

        etixConfirm.setOnClickListener(view -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(EticketConfirmation.this, R.style.CUSTOM_ALERT);
            alert.setTitle("E-tickets can't be cancelled or changed.");
            alert.setMessage("Be sure this is the movie, showtime, and theater you want to attend before proceeding");
            alert.setNegativeButton(android.R.string.cancel, (dialog, which) -> {

            });
            alert.setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                if (seatObject != null) {
                    progressWheel.setVisibility(View.VISIBLE);
                    reserve(screeningObject, selectedShowTime, seatObject);
                } else {
                    progressWheel.setVisibility(View.VISIBLE);
                    reserve(screeningObject, selectedShowTime, null);
                }
            }));
            alert.show();


        });
    }

    public void reserve(Screening screening, String showtime, @Nullable SeatSelected seatSelected) {
//        Location mCurrentLocation = UserLocationManagerFused.getLocationInstance(this).mCurrentLocation;
//        UserLocationManagerFused.getLocationInstance(this).updateLocation(mCurrentLocation);

        @Nullable final SelectedSeat selectedSeat;
        if (seatSelected == null) {
            selectedSeat = null;
        } else {
            selectedSeat = new SelectedSeat(seatSelected.getSelectedSeatRow(), seatSelected.getSelectedSeatColumn());
        }

        Availability availability = screening.getAvailability(showtime);
        //TicketInfoRequest ticketInfoRequest = new TicketInfoRequest(availability.getProviderInfo(), selectedSeat, null, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

       // reservationRequest(screening, ticketInfoRequest, showtime);
    }


    private void reservationRequest(final Screening screening, TicketInfoRequest request, final String showtime) {
        UserPreferences.setLastCheckInAttemptDate();
        RestClient.getAuthenticated().checkIn(request).enqueue(new RestCallback<ReservationResponse>() {
            @Override
            public void onResponse(Call<ReservationResponse> call, Response<ReservationResponse> response) {
                ReservationResponse reservationResponse = response.body();

                if (reservationResponse != null && reservationResponse.isOk()) {
                    progressWheel.setVisibility(View.GONE);
                    Reservation reservation = reservationResponse.getReservation();
                    UserPreferences.saveReservation(new ScreeningToken(screening, reservationResponse.getShowtime(), reservation, theater));

                    ScreeningToken token = new ScreeningToken(screening, showtime, reservation, reservationResponse.getETicketConfirmation(), null, theater);

                    showConfirmation(token);

                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(EticketConfirmation.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        finish();
                        progressWheel.setVisibility(View.GONE);
                    } catch (Exception e) {
                        Toast.makeText(EticketConfirmation.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                        progressWheel.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void failure(RestError restError) {
                progressWheel.setVisibility(View.GONE);
                String hostname = "Unable to resolve host: No address associated with hostname";

                if (restError != null && restError.getMessage() != null && restError.getMessage().toLowerCase().contains("none.get")) {
                    Toast.makeText(EticketConfirmation.this, R.string.error, Toast.LENGTH_LONG).show();
                }
                if (restError != null && restError.getMessage() != null && restError.getMessage().toLowerCase().contains(hostname.toLowerCase())) {
                    Toast.makeText(EticketConfirmation.this, R.string.data_connection, Toast.LENGTH_LONG).show();
                }
                if (restError != null && restError.getMessage() != null && restError.getMessage().toLowerCase().matches("You have a pending reservation")) {
                    Toast.makeText(EticketConfirmation.this, R.string.pending_reservation, Toast.LENGTH_LONG).show();
                } else if (restError != null) {
                    LogUtils.newLog("resResponse:", "else onfail:" + "onRespnse fail");
                    //TODO Check why null sometimes?
//                    Toast.makeText(getActivity(), restError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showConfirmation(ScreeningToken token) {
        startActivity(ReservationActivity.Companion.newInstance(this, token).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(EticketConfirmation.this);
        stackBuilder.addParentStack(HomeActivity.class);
        stackBuilder.addNextIntent(new Intent(getApplicationContext(), HomeActivity.class));
        stackBuilder.addNextIntentWithParentStack(ReservationActivity.Companion.newInstance(EticketConfirmation.this, token));
        stackBuilder.startActivities();
    }
}
