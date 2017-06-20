package com.moviepass.activities;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.moviepass.R;
import com.moviepass.ScreeningPosterClickListener;
import com.moviepass.ShowtimeClickListener;
import com.moviepass.UserLocationManagerFused;
import com.moviepass.UserPreferences;
import com.moviepass.adapters.TheaterMoviesAdapter;
import com.moviepass.adapters.TheaterShowtimesAdapter;
import com.moviepass.helpers.BottomNavigationViewHelper;
import com.moviepass.model.PerformanceInfo;
import com.moviepass.model.Screening;
import com.moviepass.model.Theater;
import com.moviepass.network.RestCallback;
import com.moviepass.network.RestClient;
import com.moviepass.network.RestError;
import com.moviepass.requests.CheckInRequest;
import com.moviepass.requests.PerformanceInfoRequest;
import com.moviepass.requests.TicketInfoRequest;
import com.moviepass.responses.ReservationResponse;
import com.moviepass.responses.ScreeningsResponse;

import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 6/8/17.
 */

public class TheaterActivity extends BaseActivity implements ScreeningPosterClickListener, ShowtimeClickListener {

    public static final String THEATER = "theater";
    public static final String SCREENING = "screening";

    TheaterMoviesAdapter mTheaterMoviesAdapter;
    TheaterShowtimesAdapter mTheaterShowtimesAdapter;

    ArrayList<Screening> mMoviesList;
    ArrayList<String> mShowtimesList;

    protected BottomNavigationView bottomNavigationView;

    Theater mTheater;
    ScreeningsResponse mScreeningsResponse;
    Screening mScreening;
    TextView mTheaterName;
    TextView mTheaterAddress;
    TextView mTheaterCityThings;
    TextView mMovieTitle;
    TextView mMovieSelectTime;
    Button mAction;
    View mProgress;
    Location mCurrentLocation;


    @BindView(R.id.recycler_view_movies)
    RecyclerView mMoviesRecyclerView;
    @BindView(R.id.recycler_view_showtimes)
    RecyclerView mShowtimesRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theater);

        bottomNavigationView = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        Bundle extras = getIntent().getExtras();
        mTheater = Parcels.unwrap(getIntent().getParcelableExtra(THEATER));

        mTheaterName = findViewById(R.id.theater_name);
        mTheaterAddress = findViewById(R.id.theater_address);
        mTheaterCityThings = findViewById(R.id.theater_city_things);
        mMovieTitle = findViewById(R.id.movie_title);
        mMovieSelectTime = findViewById(R.id.movie_select_time);
        mAction = findViewById(R.id.button_action);
        mProgress = findViewById(R.id.progress);

        mTheaterName.setText(mTheater.getName());
        mTheaterAddress.setText(mTheater.getAddress());
        String cityThings = (mTheater.getCity() + " " + mTheater.getState() + ", " + mTheater.getZip());
        mTheaterCityThings.setText(cityThings);

        mMoviesList = new ArrayList<>();
        mShowtimesList = new ArrayList<>();

        /* Start Location Tasks */
        UserLocationManagerFused.getLocationInstance(this).startLocationUpdates();

        /* Movies RecyclerView */
        LinearLayoutManager moviesLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        mMoviesRecyclerView = findViewById(R.id.recycler_view_movies);
        mMoviesRecyclerView.setLayoutManager(moviesLayoutManager);

        mTheaterMoviesAdapter = new TheaterMoviesAdapter(mMoviesList, this);

        mMoviesRecyclerView.setAdapter(mTheaterMoviesAdapter);

        loadMovies();

        /* Showtimes RecyclerView */

        mShowtimesRecyclerView = findViewById(R.id.recycler_view_showtimes);
        mShowtimesRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
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

    private void loadMovies() {
        int theaterId = mTheater.getTribuneTheaterId();

        RestClient.getAuthenticated().getScreeningsForTheater(theaterId).enqueue(new Callback<ScreeningsResponse>() {
            @Override
            public void onResponse(Call<ScreeningsResponse> call, Response<ScreeningsResponse> response) {
                mScreeningsResponse = response.body();

                if (mScreeningsResponse != null) {

                    mMoviesList.clear();

                    if (mMoviesRecyclerView != null) {
                        mMoviesRecyclerView.getRecycledViewPool().clear();
                        mTheaterMoviesAdapter.notifyDataSetChanged();
                    }

                    mMoviesList.addAll(mScreeningsResponse.getScreenings());

                } else {
                    /* TODO : FIX IF RESPONSE IS NULL */
                    Log.d("else", "else" + response.message());
                }
            }

            @Override
            public void onFailure(Call<ScreeningsResponse> call, Throwable t) {
                Log.d("t", t.getMessage());
            }
        });
    }

    public void onScreeningPosterClick(int pos, Screening screening, List<String> startTimes, ImageView sharedImageView) {
        /* TODO : Get the screenings for that movie */

        mTheaterShowtimesAdapter = new TheaterShowtimesAdapter(mShowtimesList, screening, this);

        mShowtimesRecyclerView.setAdapter(mTheaterShowtimesAdapter);

        String atWhatTime = getResources().getString(R.string.activity_theater_movie_time);
        mMovieSelectTime.setText(screening.getTitle() + " " + atWhatTime);

        mMovieSelectTime.setVisibility(View.VISIBLE);
        mShowtimesRecyclerView.setVisibility(View.VISIBLE);

        mShowtimesRecyclerView.requestFocus();

        mShowtimesList.clear();

        if (mShowtimesRecyclerView != null) {
            mShowtimesRecyclerView.getRecycledViewPool().clear();
            mTheaterShowtimesAdapter.notifyDataSetChanged();
        }

        mShowtimesList.addAll(startTimes);

        /* TODO change it so it passes the screening & the showtimes separartely to retrieve for checkin */
    }

    public void onShowtimeClick(int pos, final Screening screening, String showtime) {
        Log.d("String", showtime);
        final String time = showtime;

        mAction.setVisibility(View.VISIBLE);
        mAction.requestFocus();

        PerformanceInfo performanceInfo = screening.getProvider().getPerformanceInfo(showtime);
        Log.d("perfInfo", screening.toString());

        String ticketType = screening.getProvider().ticketType;

        if (ticketType.matches("STANDARD")) {
            String checkIn = "Check In";
            mAction.setText(checkIn);
        } else if (ticketType.matches("")) {
            String reserve = "Reserve E-Ticket";
            mAction.setText(reserve);
        } else {
            String selectSeat = "Select Seat";
            mAction.setText(selectSeat);
        }

        mAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reserve(screening, time);
            }
        });
    }


    public void reserve(Screening screening, String showtime) {
        mAction.setEnabled(false);

        Location mCurrentLocation = UserLocationManagerFused.getLocationInstance(this).mCurrentLocation;
        UserLocationManagerFused.getLocationInstance(this).updateLocation(mCurrentLocation);

        /* Standard Check In */

        String providerName = screening.getProvider().providerName;

        //PerformanceInfo
        int normalizedMovieId = screening.getProvider().getPerformanceInfo(showtime).getNormalizedMovieId();
        String externalMovieId = screening.getProvider().getPerformanceInfo(showtime).getExternalMovieId();
        String format = screening.getProvider().getPerformanceInfo(showtime).getFormat();
        int tribuneTheaterId = screening.getProvider().getPerformanceInfo(showtime).getTribuneTheaterId();
        int screeningId = screening.getProvider().getPerformanceInfo(showtime).getScreeningId();
        String dateTime = screening.getProvider().getPerformanceInfo(showtime).getDateTime();


        PerformanceInfoRequest performanceInfo = new PerformanceInfoRequest(normalizedMovieId, externalMovieId, format, tribuneTheaterId, screeningId, dateTime);

        TicketInfoRequest ticketInfo = new TicketInfoRequest(performanceInfo);

        CheckInRequest checkInRequest = new CheckInRequest(ticketInfo, providerName, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        RestClient.getAuthenticated().checkIn(checkInRequest).enqueue(new RestCallback<ReservationResponse>() {
            @Override
            public void onResponse(Call<ReservationResponse> call, Response<ReservationResponse> response) {
                ReservationResponse reservationResponse = response.body();
                if (reservationResponse != null && reservationResponse.isOk()) {
//                    mToken.setReservation(reservationResponse.getReservation());
//                    mToken.setZipCodeTicket(reservationResponse.getZipCode());
                    mProgress.setVisibility(View.GONE);

                    showConfirmation();


/*                    if (!UserPreferences.getVerificationRequired()) {
                        showConfirmation();
                    } else {
                        showTicketVerificationConfirmation();
                    } */

                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());

                        //PENDING RESERVATION GO TO TicketConfirmationActivity or TicketVerificationActivity
                        if (jObjError.getString("message").matches("You have a pending reservation")) {
                            mProgress.setVisibility(View.GONE);
                            mAction.setEnabled(true);
                            Toast.makeText(TheaterActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(TheaterActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                            mProgress.setVisibility(View.GONE);
                            mAction.setEnabled(true);
                        }
                    } catch (Exception e) {
                        Toast.makeText(TheaterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    Log.d("resResponse:", "else onResponse:" + "onRespnse fail");
                    mProgress.setVisibility(View.GONE);
                    mAction.setEnabled(true);
                }

                mAction.setEnabled(true);
            }

            @Override
            public void failure(RestError restError) {
                mProgress.setVisibility(View.GONE);
                mAction.setEnabled(true);

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

    private void showConfirmation() {
        Intent confirmationIntent = new Intent(TheaterActivity.this, ConfirmationActivity.class);
        confirmationIntent.putExtra(SCREENING, Parcels.wrap(mScreening));
        startActivity(confirmationIntent);
        finish();
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_browse;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.action_browse;
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
                    Toast.makeText(TheaterActivity.this, "E-Ticket Activity", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), ETicketsActivity.class));
                } else if (itemId == R.id.action_browse) {
                } else if (itemId == R.id.action_notifications) {
                    Toast.makeText(TheaterActivity.this, "Notification Activity", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), NotificationsActivity.class));
                } else if (itemId == R.id.action_settings) {
                    Toast.makeText(TheaterActivity.this, "Settings Activity", Toast.LENGTH_LONG).show();
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
