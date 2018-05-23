package com.mobile.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.helpshift.support.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mobile.Constants;
import com.mobile.UserLocationManagerFused;
import com.mobile.UserPreferences;
import com.mobile.helpers.BottomNavigationViewHelper;
import com.mobile.helpers.LogUtils;
import com.mobile.model.Reservation;
import com.mobile.model.Screening;
import com.mobile.model.ScreeningToken;
import com.mobile.model.SeatSelected;
import com.mobile.model.Theater;
import com.mobile.network.RestCallback;
import com.mobile.network.RestClient;
import com.mobile.network.RestError;
import com.mobile.requests.CheckInRequest;
import com.mobile.requests.PerformanceInfoRequest;
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

    TextView etixTitle, etixTheater, etixShowtime, etixSeat, seatTExt;
    SimpleDraweeView etixPoster;
    ImageView etixOnBack;
    Screening screeningObject;
    SeatSelected seatObject;
    Button etixConfirm;
    String selectedShowTime;
    View progressWheel;
    RelativeLayout relSeat;
    String providerName;
    TicketInfoRequest ticketRequest;
    CheckInRequest checkinRequest;
    Theater theater;
    PerformanceInfoRequest mPerformReq;
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

        bottomNavigationView = findViewById(R.id.ETIX_NAV);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        //set details for confirmation page..

        Intent intent = getIntent();
        screeningObject = Parcels.unwrap(intent.getParcelableExtra(SCREENING));
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
                    reserveWithSeat(screeningObject, selectedShowTime, seatObject);
                } else {
                    progressWheel.setVisibility(View.VISIBLE);
                    reserveNoSeat(screeningObject, selectedShowTime);
                }
            }));
            alert.show();


        });
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
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                } else if (itemId == R.id.action_movies) {
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


    public void reserveWithSeat(Screening screening, String showtime, SeatSelected seatSelected) {
        Location mCurrentLocation = UserLocationManagerFused.getLocationInstance(this).mCurrentLocation;
        UserLocationManagerFused.getLocationInstance(this).updateLocation(mCurrentLocation);

        SelectedSeat selectedSeat = new SelectedSeat(seatSelected.getSelectedSeatRow(), seatSelected.getSelectedSeatColumn());


        if (screening.getProvider().getProviderName().equalsIgnoreCase("MOVIEXCHANGE")) {
            int normalizedMovieId = screening.getMoviepassId();
            String externalMovieId = screening.getProvider().getPerformanceInfo(showtime).getExternalMovieId();
            String format = screening.getFormat();
            int tribuneTheaterId = screening.getTribuneTheaterId();
            int screeningId = screening.getProvider().getPerformanceInfo(showtime).getScreeningId();
            int performanceNumber = screening.getProvider().getPerformanceInfo(showtime).getPerformanceNumber();
            String sku = screening.getProvider().getPerformanceInfo(showtime).getSku();
            Double price = screening.getProvider().getPerformanceInfo(showtime).getPrice();
            String dateTime = screening.getProvider().getPerformanceInfo(showtime).getDateTime();
            String auditorium = screening.getProvider().getPerformanceInfo(showtime).getAuditorium();
            String performanceId = screening.getProvider().getPerformanceInfo(showtime).getPerformanceId();
            String sessionId = screening.getProvider().getPerformanceInfo(showtime).getSessionId();
            int theater = screening.getProvider().getTheater();
            String cinemaChainId = screening.getProvider().getPerformanceInfo(showtime).getCinemaChainId();
            String showtimeId = screening.getProvider().getPerformanceInfo(showtime).getShowtimeId();
            TicketType ticketType = screening.getProvider().getPerformanceInfo(showtime).getTicketType();


            PerformanceInfoRequest perform = new PerformanceInfoRequest(
                    normalizedMovieId,
                    externalMovieId,
                    format,
                    tribuneTheaterId,
                    screeningId,
                    dateTime,
                    performanceNumber,
                    sku,
                    price,
                    auditorium,
                    performanceId,
                    sessionId,
                    cinemaChainId,
                    ticketType,
                    showtimeId);

            ticketRequest = new TicketInfoRequest(perform, selectedSeat);


        } else {
            //IF not movieXchange then it will simply request these parameters:
            int normalizedMovieId = screening.getMoviepassId();
            String externalMovieId = screening.getProvider().getPerformanceInfo(showtime).getExternalMovieId();
            String format = screening.getFormat();
            int tribuneTheaterId = screening.getTribuneTheaterId();
            int performanceNumber = screening.getProvider().getPerformanceInfo(showtime).getPerformanceNumber();
            String sku = screening.getProvider().getPerformanceInfo(showtime).getSku();
            Double price = screening.getProvider().getPerformanceInfo(showtime).getPrice();
            String dateTime = screening.getProvider().getPerformanceInfo(showtime).getDateTime();
            String auditorium = screening.getProvider().getPerformanceInfo(showtime).getAuditorium();
            String performanceId = screening.getProvider().getPerformanceInfo(showtime).getPerformanceId();
            String sessionId = screening.getProvider().getPerformanceInfo(showtime).getSessionId();
//            int theater = screeningObject.getProvider().getTheater();

            PerformanceInfoRequest request = new PerformanceInfoRequest(
                    dateTime,
                    externalMovieId,
                    performanceNumber,
                    tribuneTheaterId,
                    format,
                    normalizedMovieId,
                    sku,
                    price,
                    auditorium,
                    performanceId,
                    sessionId);
            ticketRequest = new TicketInfoRequest(request, selectedSeat);
            LogUtils.newLog(Constants.TAG, "performinfo:2 " + selectedSeat);

        }

        providerName = screening.getProvider().providerName;
        checkinRequest = new CheckInRequest(ticketRequest, providerName, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        reservationRequest(screening, checkinRequest, showtime, seatSelected);
    }


    private void reservationRequest(final Screening screening, CheckInRequest checkInRequest, final String showtime, SeatSelected seatSelected) {
        UserPreferences.setLastCheckInAttemptDate();
        RestClient.getAuthenticated().checkIn(checkInRequest).enqueue(new RestCallback<ReservationResponse>() {
            @Override
            public void onResponse(Call<ReservationResponse> call, Response<ReservationResponse> response) {
                ReservationResponse reservationResponse = response.body();

                SeatSelected seat = seatSelected;
                if (reservationResponse != null && reservationResponse.isOk()) {
                    progressWheel.setVisibility(View.GONE);
                    Reservation reservation = reservationResponse.getReservation();
                    UserPreferences.saveReservation(reservation);

                    ScreeningToken token = new ScreeningToken(screening, showtime, reservation, reservationResponse.getE_ticket_confirmation(), seat, null);
                    LogUtils.newLog(Constants.TAG, "onResponse: " + seat.getSeatName());

                    showConfirmation(token);
                    finish();

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
        startActivity(ReservationActivity.Companion.newInstance(this, token));
    }


    public void reserveNoSeat(Screening screening, String showtime) {
        Location mCurrentLocation = UserLocationManagerFused.getLocationInstance(this).mCurrentLocation;
        UserLocationManagerFused.getLocationInstance(this).updateLocation(mCurrentLocation);


        if (screening.getProvider().getProviderName().equalsIgnoreCase("MOVIEXCHANGE")) {
            int normalizedMovieId = screening.getMoviepassId();
            String externalMovieId = screening.getProvider().getPerformanceInfo(showtime).getExternalMovieId();
            String format = screening.getFormat();
            int tribuneTheaterId = screening.getTribuneTheaterId();
            int screeningId = screening.getProvider().getPerformanceInfo(showtime).getScreeningId();
            int performanceNumber = screening.getProvider().getPerformanceInfo(showtime).getPerformanceNumber();
            String sku = screening.getProvider().getPerformanceInfo(showtime).getSku();
            Double price = screening.getProvider().getPerformanceInfo(showtime).getPrice();
            String dateTime = screening.getProvider().getPerformanceInfo(showtime).getDateTime();
            String auditorium = screening.getProvider().getPerformanceInfo(showtime).getAuditorium();
            String performanceId = screening.getProvider().getPerformanceInfo(showtime).getPerformanceId();
            String sessionId = screening.getProvider().getPerformanceInfo(showtime).getSessionId();
            int theater = screening.getProvider().getTheater();
            String cinemaChainId = screening.getProvider().getPerformanceInfo(showtime).getCinemaChainId();
            String showtimeId = screening.getProvider().getPerformanceInfo(showtime).getShowtimeId();
            TicketType ticketType = screening.getProvider().getPerformanceInfo(showtime).getTicketType();


            PerformanceInfoRequest perform = new PerformanceInfoRequest(
                    normalizedMovieId,
                    externalMovieId,
                    format,
                    tribuneTheaterId,
                    screeningId,
                    dateTime,
                    performanceNumber,
                    sku,
                    price,
                    auditorium,
                    performanceId,
                    sessionId,
                    cinemaChainId,
                    ticketType,
                    showtimeId);

            ticketRequest = new TicketInfoRequest(perform);


        } else {
            //IF not movieXchange then it will simply request these parameters:
            int normalizedMovieId = screening.getMoviepassId();
            String externalMovieId = screening.getProvider().getPerformanceInfo(showtime).getExternalMovieId();
            String format = screening.getFormat();
            int tribuneTheaterId = screening.getTribuneTheaterId();
            int performanceNumber = screening.getProvider().getPerformanceInfo(showtime).getPerformanceNumber();
            String sku = screening.getProvider().getPerformanceInfo(showtime).getSku();
            Double price = screening.getProvider().getPerformanceInfo(showtime).getPrice();
            String dateTime = screening.getProvider().getPerformanceInfo(showtime).getDateTime();
            String auditorium = screening.getProvider().getPerformanceInfo(showtime).getAuditorium();
            String performanceId = screening.getProvider().getPerformanceInfo(showtime).getPerformanceId();
            String sessionId = screening.getProvider().getPerformanceInfo(showtime).getSessionId();

            PerformanceInfoRequest request = new PerformanceInfoRequest(
                    dateTime,
                    externalMovieId,
                    performanceNumber,
                    tribuneTheaterId,
                    format,
                    normalizedMovieId,
                    sku,
                    price,
                    auditorium,
                    performanceId,
                    sessionId);
            ticketRequest = new TicketInfoRequest(request);


        }

        providerName = screening.getProvider().providerName;
        checkinRequest = new CheckInRequest(ticketRequest, providerName, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        reservationRequestNoSeat(screening, checkinRequest, showtime);
    }

    private void reservationRequestNoSeat(final Screening screening, CheckInRequest checkInRequest, final String showtime) {
        RestClient.getAuthenticated().checkIn(checkInRequest).enqueue(new RestCallback<ReservationResponse>() {
            @Override
            public void onResponse(Call<ReservationResponse> call, Response<ReservationResponse> response) {
                ReservationResponse reservationResponse = response.body();

                if (reservationResponse != null && reservationResponse.isOk()) {
                    progressWheel.setVisibility(View.GONE);
                    Reservation reservation = reservationResponse.getReservation();

                    ReservationResponse.ETicketConfirmation confirmationCode = reservationResponse.getE_ticket_confirmation();

                    ScreeningToken token = new ScreeningToken(screening, showtime, reservation, confirmationCode, theater);

                    showConfirmation(token);
                    finish();

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
                    Toast.makeText(EticketConfirmation.this, restError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
