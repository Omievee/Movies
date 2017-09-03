package com.moviepass.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.moviepass.Constants;
import com.moviepass.R;
import com.moviepass.UserLocationManagerFused;
import com.moviepass.UserPreferences;
import com.moviepass.extensions.SeatButton;
import com.moviepass.helpers.BottomNavigationViewHelper;
import com.moviepass.model.Movie;
import com.moviepass.model.Reservation;
import com.moviepass.model.Screening;
import com.moviepass.model.ScreeningToken;
import com.moviepass.model.SeatInfo;
import com.moviepass.model.SelectedSeat;
import com.moviepass.model.Theater;
import com.moviepass.network.RestCallback;
import com.moviepass.network.RestClient;
import com.moviepass.network.RestError;
import com.moviepass.requests.CheckInRequest;
import com.moviepass.requests.PerformanceInfoRequest;
import com.moviepass.requests.SelectedSeatRequest;
import com.moviepass.requests.TicketInfoRequest;
import com.moviepass.responses.ReservationResponse;
import com.moviepass.responses.SeatingsInfoResponse;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 6/27/17.
 */

public class SelectSeatActivity extends BaseActivity {

    public static final String MOVIE = "movie";
    public static final String SCREENING = "screening" ;
    public static final String SHOWTIME = "showtime";
    public static final String THEATER = "theater";
    public static final String TOKEN = "token";

    LocationUpdateBroadCast mLocationBroadCast;
    boolean mLocationAcquired;
    private Location mMyLocation;

    RelativeLayout relativeLayout;
    GridLayout gridSeats;
    ImageView poster;
    Screening screening;
    TextView movieTitle;
    TextView movieRunTime;
    TextView theaterName;
    TextView screeningShowtime;
    TextView selectedSeatText;
    Button buttonAction;
    View progress;

    private ArrayList<SeatButton> seatButtons;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_seat);

        final Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        final ActionBar mActionBar = getSupportActionBar();

        // Enable the Up button
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);

        mToolbar.setTitle(R.string.activity_select_seat_activity_title);
        mActionBar.setTitle(R.string.activity_select_seat_activity_title);

        bottomNavigationView = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        screening = Parcels.unwrap(getIntent().getParcelableExtra(SCREENING));
        String showtime = getIntent().getStringExtra(SHOWTIME);

        relativeLayout = findViewById(R.id.relative_layout);
        poster = findViewById(R.id.poster);
        movieTitle = findViewById(R.id.movie_title);
        movieRunTime = findViewById(R.id.text_run_time);
        theaterName = findViewById(R.id.theater_name);
        screeningShowtime = findViewById(R.id.showtime);
        selectedSeatText = findViewById(R.id.selected_seats);
        gridSeats = findViewById(R.id.grid_seats);
        buttonAction = findViewById(R.id.button_action);
        progress = findViewById(R.id.progress);

        Picasso.with(this)
                .load(screening.getImageUrl())
                .error(R.mipmap.ic_launcher)
                .into(poster);

        movieTitle.setText(screening.getTitle());

        int t = screening.getRunningTime();
        int hours = t / 60; //since both are ints, you get an int
        int minutes = t % 60;

        if (screening.getRunningTime() == 0) {
            movieRunTime.setVisibility(View.GONE);
        } else if (hours > 1) {
            String translatedRunTime = hours + " hours " + minutes + " minutes";
            movieRunTime.setText(translatedRunTime);
        } else {
            String translatedRunTime = hours + " hour " + minutes + " minutes";
            movieRunTime.setText(translatedRunTime);
        }

        theaterName.setText(screening.getTheaterName());
        screeningShowtime.setText(showtime);

        buttonAction.setText(R.string.activity_select_seat_activity_title);

        //PerformanceInfo
        int normalizedMovieId = screening.getMoviepassId();
        Log.d("showtime", showtime);
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
        int theater = screening.getProvider().getTheater();

        PerformanceInfoRequest performanceInfoRequest =  new PerformanceInfoRequest(dateTime, externalMovieId, performanceNumber,
                tribuneTheaterId, format, normalizedMovieId, sku, price, auditorium, performanceId, sessionId);

        getSeats(tribuneTheaterId, theater, performanceInfoRequest);

        //If seat hasn't been selected return error
        if (buttonAction.getText().toString().matches(getString(R.string.activity_select_seat_activity_title))) {
            buttonAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (buttonAction.getText().toString().matches(getString(R.string.activity_select_seat_activity_title))) {
                        makeSnackbar(getString(R.string.activity_select_seat_select_first));
                    }
                }
            });
        }
    }

    protected void getSeats(int tribuneTheaterId, int theater, PerformanceInfoRequest performanceInfoRequest) {
        progress.setVisibility(View.VISIBLE);

        RestClient.getAuthenticated().getSeats(tribuneTheaterId,
                String.valueOf(theater), performanceInfoRequest).enqueue(
                new Callback<SeatingsInfoResponse>() {
                    @Override
                    public void onResponse(Call<SeatingsInfoResponse> call, Response<SeatingsInfoResponse> response) {
                        progress.setVisibility(View.GONE);

                        SeatingsInfoResponse seatingsInfoResponse = response.body();
                        if (seatingsInfoResponse != null) {
                            showSeats(seatingsInfoResponse.seatingInfo.seats, seatingsInfoResponse.seatingInfo.rows, seatingsInfoResponse.seatingInfo.columns);
                        }

                    }

                    @Override
                    public void onFailure(Call<SeatingsInfoResponse> call, Throwable t) {
                        progress.setVisibility(View.GONE);
                        Log.d("error", "Unable to download seat information: " + t.getMessage().toString());
                    }
                });
    }

    private void showSeats(List<SeatInfo> seats, int rows, int columns) {
        gridSeats.setBackgroundColor(Color.TRANSPARENT);
        gridSeats.setColumnCount(columns);
        gridSeats.setRowCount(rows);
        gridSeats.setOrientation(GridLayout.HORIZONTAL);

        seatButtons = new ArrayList<>();

        final String showtime = getIntent().getStringExtra(SHOWTIME);

        Collections.sort(seats);
        for (SeatInfo seat : seats) {
            SeatButton seatButton = new SeatButton(this, seat);

            final int seatRow = seat.getRow();
            final int seatCol = seat.getColumn();
            final String finalSeatName = seat.getSeatName();

            seatButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View sender) {
                    Log.d("seat", "seat: " + seatRow + " col:" + seatCol);

                    if (finalSeatName != null) {
                        selectedSeatText.setText(finalSeatName);
                    } else {
                        String formattedSeatName = "Row: " + seatCol + " Seat: " + seatRow;
                        selectedSeatText.setText(formattedSeatName);
                    }

                    final SeatButton button = (SeatButton) sender;

                    selectSeat(button.getSeatName());

//                    mToken.setSeatName(button.getSeatName());

                    buttonAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            SelectedSeat selectedSeat = new SelectedSeat(button.getSeatInfo().getRow(), button.getSeatInfo().getColumn(), button.getSeatName());

                            reserve(screening, showtime, selectedSeat);
                            progress.setVisibility(View.VISIBLE);
                            buttonAction.setEnabled(false);
                        }
                    });
                }
            });

            seatButton.setPadding(3, 3, 3, 3);
            gridSeats.addView(seatButton);
            seatButtons.add(seatButton);
        }
    }

    private void selectSeat(String seatName) {
        for (SeatButton button : seatButtons) {
            button.setSeatSelected(button.getSeatName().matches(seatName));
            buttonAction.setText(R.string.activity_select_seat_reserve);
        }
    }

    private void reserve(Screening screening, String showtime, SelectedSeat selectedSeat) {

        Location mCurrentLocation = UserLocationManagerFused.getLocationInstance(this).mCurrentLocation;
        UserLocationManagerFused.getLocationInstance(this).updateLocation(mCurrentLocation);

        String providerName = screening.getProvider().providerName;

        //PerformanceInfo
        int normalizedMovieId = screening.getProvider().getPerformanceInfo(showtime).getNormalizedMovieId();
        String externalMovieId = screening.getProvider().getPerformanceInfo(showtime).getExternalMovieId();
        String format = screening.getProvider().getPerformanceInfo(showtime).getFormat();
        int tribuneTheaterId = screening.getProvider().getPerformanceInfo(showtime).getTribuneTheaterId();
        int screeningId = screening.getProvider().getPerformanceInfo(showtime).getScreeningId();
        String dateTime = screening.getProvider().getPerformanceInfo(showtime).getDateTime();
        String auditorium = screening.getProvider().getPerformanceInfo(showtime).getAuditorium();
        String performanceId = screening.getProvider().getPerformanceInfo(showtime).getPerformanceId();
        String sessionId = screening.getProvider().getPerformanceInfo(showtime).getSessionId();
        int performanceNumber = screening.getProvider().getPerformanceInfo(showtime).getPerformanceNumber();
        String sku = screening.getProvider().getPerformanceInfo(showtime).getSku();
        Double price = screening.getProvider().getPerformanceInfo(showtime).getPrice();
        SelectedSeatRequest selectedSeatRequest = new SelectedSeatRequest(selectedSeat.getSelectedSeatRow(), selectedSeat.getSelectedSeatColumn());

        PerformanceInfoRequest performanceInfo = new PerformanceInfoRequest(dateTime, externalMovieId, performanceNumber,
                tribuneTheaterId, format, normalizedMovieId, sku, price, auditorium, performanceId, sessionId);
        TicketInfoRequest ticketInfo = new TicketInfoRequest(performanceInfo, selectedSeatRequest);
        CheckInRequest checkInRequest = new CheckInRequest(ticketInfo, providerName, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        reservationRequest(screening, checkInRequest, showtime, selectedSeat);
    }

    private void reservationRequest(final Screening screening, CheckInRequest checkInRequest, final String showtime, final SelectedSeat selectedSeat) {
        RestClient.getAuthenticated().checkIn(checkInRequest).enqueue(new RestCallback<ReservationResponse>() {
            @Override
            public void onResponse(Call<ReservationResponse> call, Response<ReservationResponse> response) {
                ReservationResponse reservationResponse = response.body();

                if (reservationResponse != null && reservationResponse.isOk()) {
                    buttonAction.setEnabled(true);
                    progress.setVisibility(View.GONE);
                    Reservation reservation = reservationResponse.getReservation();

                    String confirmationCode = reservationResponse.getE_ticket_confirmation().getConfirmationCode();
                    String qrUrl = reservationResponse.getE_ticket_confirmation().getBarCodeUrl();

                    ScreeningToken token = new ScreeningToken(screening, showtime, reservation, qrUrl, confirmationCode, selectedSeat);

                    showConfirmation(token);

                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());

                        Toast.makeText(SelectSeatActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        progress.setVisibility(View.GONE);
                        buttonAction.setEnabled(true);
                    } catch (Exception e) {
                        Toast.makeText(SelectSeatActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        progress.setVisibility(View.GONE);
                        buttonAction.setEnabled(true);
                    }
                }

            }

            @Override
            public void failure(RestError restError) {
                progress.setVisibility(View.GONE);
                buttonAction.setEnabled(true);

                String hostname = "Unable to resolve host: No address associated with hostname";

/*                if (restError != null && restError.getMessage() != null && restError.getMessage().toLowerCase().contains("none.get")) {
                    Toast.makeText(TheaterActivity.this, R.string.log_out_log_in, Toast.LENGTH_LONG).show();
                }
                if (restError != null && restError.getMessage() != null && restError.getMessage().toLowerCase().contains(hostname.toLowerCase())) {
                    Toast.makeText(TheaterActivity.this, R.string.data_connection, Toast.LENGTH_LONG).show();
                }
                if (restError != null && restError.getMessage() != null && restError.getMessage().toLowerCase().matches("You have a pending reservation")) {
                    Toast.makeText(TheaterActivity.this, "Pending Reservation", Toast.LENGTH_LONG).show();
                } else if(restError!=null){
                    Log.d("resResponse:", "else onfail:" + "onRespnse fail");
                    Toast.makeText(TheaterActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
                }
                clearSuccessCount(); */
            }
        });
    }

    private void showConfirmation(ScreeningToken token) {
        Intent confirmationIntent = new Intent(SelectSeatActivity.this, ConfirmationActivity.class);
        confirmationIntent.putExtra(TOKEN, Parcels.wrap(token));
        startActivity(confirmationIntent);
        finish();
    }

    class LocationUpdateBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (mLocationBroadCast != null) {
                    unregisterReceiver(mLocationBroadCast);
                }
            } catch (IllegalArgumentException is) {
                is.printStackTrace();
            }
            UserLocationManagerFused.getLocationInstance(SelectSeatActivity.this).stopLocationUpdates();
            onLocationChanged(UserLocationManagerFused.getLocationInstance(context).mCurrentLocation);
        }
    }

    protected void onLocationChanged(Location location) {
        UserLocationManagerFused.getLocationInstance(this).stopLocationUpdates();

        if (location != null) {
            UserLocationManagerFused.getLocationInstance(this).updateLocation(location);

            mMyLocation = location;

            mLocationAcquired = true;
        }
    }

    public void currentLocationTasks() {
        registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));
        UserLocationManagerFused.getLocationInstance(SelectSeatActivity.this).startLocationUpdates();
        mLocationAcquired = false;

        boolean enabled = UserLocationManagerFused.getLocationInstance(SelectSeatActivity.this).isLocationEnabled();
        if (!enabled) {
//            showDialogGPS();
        } else {
            Location location = UserLocationManagerFused.getLocationInstance(SelectSeatActivity.this).mCurrentLocation;
            onLocationChanged(location);

            if (location != null) {
                UserLocationManagerFused.getLocationInstance(this).requestLocationForCoords(location.getLatitude(), location.getLongitude(), SelectSeatActivity.this);
            }
        }
    }

    public void makeSnackbar(String message) {
        final Snackbar snackbar = Snackbar.make(relativeLayout, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getParcelableExtra(THEATER) != null) {
            Theater theater = Parcels.unwrap(getIntent().getParcelableExtra(THEATER));

            Intent intent = new Intent(SelectSeatActivity.this, TheaterActivity.class);
            intent.putExtra(TheaterActivity.THEATER, Parcels.wrap(theater));
            startActivity(intent);
        } else if (getIntent().getParcelableExtra(MOVIE) != null){
            Movie movie = Parcels.unwrap(getIntent().getParcelableExtra(MOVIE));

            Intent intent = new Intent(SelectSeatActivity.this, MovieActivity.class);
            intent.putExtra(TheaterActivity.THEATER, Parcels.wrap(movie));
            startActivity(intent);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    /* Bottom Navigation Things */

    int getContentViewId() {
        return R.layout.activity_browse;
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
                } else if (itemId == R.id.action_reservations) {
                    Toast.makeText(SelectSeatActivity.this, "E-Ticket Activity", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), ReservationsActivity.class));
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
