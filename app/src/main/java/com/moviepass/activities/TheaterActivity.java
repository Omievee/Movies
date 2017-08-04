package com.moviepass.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.listeners.ScreeningPosterClickListener;
import com.moviepass.listeners.ShowtimeClickListener;
import com.moviepass.UserLocationManagerFused;
import com.moviepass.adapters.TheaterMoviesAdapter;
import com.moviepass.adapters.TheaterShowtimesAdapter;
import com.moviepass.helpers.BottomNavigationViewHelper;
import com.moviepass.model.Reservation;
import com.moviepass.model.Screening;
import com.moviepass.model.ScreeningToken;
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

    public static final String EXTRA_CIRCULAR_REVEAL_TRANSITION_NAME = "circular_reveal_transition_name";
    public static final String THEATER = "theater";
    public static final String RESERVATION = "reservation";
    public static final String SCREENING = "screening";
    public static final String SHOWTIME = "showtime";
    public static final String TOKEN = "token";

    TheaterMoviesAdapter mTheaterMoviesAdapter;
    TheaterShowtimesAdapter mTheaterShowtimesAdapter;

    ArrayList<Screening> mMoviesList;
    ArrayList<String> mShowtimesList;

    protected BottomNavigationView bottomNavigationView;

    Theater mTheater;
    ScreeningsResponse mScreeningsResponse;
    Screening mScreening;
    Reservation reservation;
    TextView mTheaterName;
    TextView mTheaterAddress;
    TextView mTheaterCityThings;
    TextView mMovieTitle;
    TextView mMovieSelectTime;
    Button mAction;
    View mProgress;
    View mRedView;
    Location mCurrentLocation;

    @BindView(R.id.recycler_view_movies)
    RecyclerView mMoviesRecyclerView;
    @BindView(R.id.recycler_view_showtimes)
    RecyclerView mShowtimesRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theater);

        final Toolbar mToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);

        final ActionBar mActionBar = getSupportActionBar();

        // Enable the Up button
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);

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
        mAction.setFocusable(true);
        mAction.setFocusableInTouchMode(true);
        mProgress = findViewById(R.id.progress);

        mTheaterName.setText(mTheater.getName());
        mTheaterAddress.setText(mTheater.getAddress());
        String cityThings = (mTheater.getCity() + " " + mTheater.getState() + ", " + mTheater.getZip());
        mTheaterCityThings.setText(cityThings);

        mToolbar.setTitle(mTheater.getName());
        mActionBar.setTitle(mTheater.getName());

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
        mMoviesRecyclerView.getViewTreeObserver().addOnPreDrawListener(
            new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mMoviesRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

                    for (int i = 0; i < mMoviesRecyclerView.getChildCount(); i++) {
                        View v = mMoviesRecyclerView.getChildAt(i);
                        v.setAlpha(0.0f);
                        v.animate().alpha(1.0f)
                                .setDuration(1000)
                                .setStartDelay(i * 50)
                                .start();
                    }

                    return true;
                }
            });

        loadMovies();

        /* Showtimes RecyclerView */

        mShowtimesRecyclerView = findViewById(R.id.recycler_view_showtimes);
        mShowtimesRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        if (Build.VERSION.SDK_INT >= 21) {
            mRedView = findViewById(R.id.red);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // previously visible view
                    // get the center for the clipping circle
                    int cx = mRedView.getWidth() / 2;
                    int cy = mRedView.getHeight() / 2;

                    Log.d("cx", String.valueOf(cx));

                    // get the initial radius for the clipping circle
                    float initialRadius = (float) Math.hypot(cx, cy);

                    // create the animation (the final radius is zero)
                    Animator anim = ViewAnimationUtils.createCircularReveal(mRedView, cx, cy, initialRadius, 0);

                    // make the view invisible when the animation is done
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mRedView.setVisibility(View.INVISIBLE);
                            mToolbar.setVisibility(View.VISIBLE);
                        }
                    });

                    anim.start();
                }

            }, 100);
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Log.d("onBackPressed", "onBackPressed");

        if (Build.VERSION.SDK_INT >= 21) {
            mRedView = findViewById(R.id.red);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // previously visible view
                    // get the center for the clipping circle
                    int cx = mRedView.getWidth() / 2;
                    int cy = mRedView.getHeight() / 2;

                    Log.d("cx", String.valueOf(cx));

                    // get the initial radius for the clipping circle
                    float initialRadius = (float) Math.hypot(cx, cy);

                    // create the animation (the final radius is zero)
                    Animator anim = ViewAnimationUtils.createCircularReveal(mRedView, cx, cy, initialRadius, 0);

                    // make the view invisible when the animation is done
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mRedView.setVisibility(View.INVISIBLE);
                        }
                    });

                    anim.start();
                }
            }, 100);
        }
    }

    public void onScreeningPosterClick(int pos, Screening screening, List<String> startTimes, ImageView sharedImageView) {
        /* TODO : Get the screenings for that movie */

        mTheaterShowtimesAdapter = new TheaterShowtimesAdapter(mShowtimesList, screening, this);

        mShowtimesRecyclerView.setAdapter(mTheaterShowtimesAdapter);
        mShowtimesRecyclerView.getViewTreeObserver().addOnPreDrawListener(
            new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    mShowtimesRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

                    for (int i = 0; i < mShowtimesRecyclerView.getChildCount(); i++) {
                        View v = mShowtimesRecyclerView.getChildAt(i);
                        v.setAlpha(0.0f);
                        v.animate().alpha(1.0f)
                                .setDuration(1000)
                                .setStartDelay(i * 50)
                                .start();
                    }

                    return true;
                }
            });

        if (mMovieSelectTime.getVisibility() == View.VISIBLE) {
            fadeOut(mMovieSelectTime);

            String atWhatTime = getResources().getString(R.string.activity_theater_movie_time);
            mMovieSelectTime.setText(screening.getTitle() + " " + atWhatTime);
            fadeIn(mMovieSelectTime);
        } else {
            mMovieSelectTime.setVisibility(View.VISIBLE);

            String atWhatTime = getResources().getString(R.string.activity_theater_movie_time);
            mMovieSelectTime.setText(screening.getTitle() + " " + atWhatTime);
            fadeIn(mMovieSelectTime);
        }

        if (mAction.getVisibility() == View.VISIBLE) {
            fadeOut(mAction);
            mAction.setVisibility(View.INVISIBLE);
        }

        mShowtimesRecyclerView.setVisibility(View.VISIBLE);
        mShowtimesRecyclerView.requestFocus();
        mShowtimesList.clear();

        if (mShowtimesRecyclerView != null) {
            mShowtimesRecyclerView.getRecycledViewPool().clear();
            mTheaterShowtimesAdapter.notifyDataSetChanged();
        }

        mShowtimesList.addAll(startTimes);
    }

    public void onShowtimeClick(int pos, final Screening screening, String showtime) {
        final String time = showtime;

        mAction.setVisibility(View.VISIBLE);

        if (mAction.getVisibility() == View.VISIBLE) {
            fadeOut(mAction);
            mAction.requestFocus();
            fadeIn(mAction);
        } else {
            fadeIn(mAction);
            mAction.setVisibility(View.VISIBLE);
            mAction.requestFocus();
        }

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
                mProgress.setVisibility(View.VISIBLE);
                reserve(screening, time);
            }
        });
    }

    public void fadeIn(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(1000);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        view.setAnimation(animation);
    }

    public void fadeOut(View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new DecelerateInterpolator()); //add this
        fadeOut.setDuration(1000);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeOut);
        view.setAnimation(animation);
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
        String auditorium = screening.getProvider().getPerformanceInfo(showtime).getAuditorium();
        String performanceId = screening.getProvider().getPerformanceInfo(showtime).getPerformanceId();
        String sessionId = screening.getProvider().getPerformanceInfo(showtime).getSessionId();
        int performanceNumber = screening.getProvider().getPerformanceInfo(showtime).getPerformanceNumber();
        String sku = screening.getProvider().getPerformanceInfo(showtime).getSku();
        Double price = screening.getProvider().getPerformanceInfo(showtime).getPrice();

        if (screening.getProvider().ticketType.matches("STANDARD")) {
            PerformanceInfoRequest performanceInfo = new PerformanceInfoRequest(dateTime, externalMovieId, performanceNumber,
                    tribuneTheaterId, format, normalizedMovieId, sku, price, auditorium, performanceId, sessionId);
            TicketInfoRequest ticketInfo = new TicketInfoRequest(performanceInfo);
            CheckInRequest checkInRequest = new CheckInRequest(ticketInfo, providerName, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            reservationRequest(screening, checkInRequest, showtime);
        } else if (screening.getProvider().ticketType.matches("E_TICKET")) {
            PerformanceInfoRequest performanceInfo = new PerformanceInfoRequest(dateTime, externalMovieId, performanceNumber,
                    tribuneTheaterId, format, normalizedMovieId, sku, price, auditorium, performanceId, sessionId);
            TicketInfoRequest ticketInfo = new TicketInfoRequest(performanceInfo);
            CheckInRequest checkInRequest = new CheckInRequest(ticketInfo, providerName, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            reservationRequest(screening, checkInRequest, showtime);
        } else {
            Intent intent = new Intent(TheaterActivity.this, SelectSeatActivity.class);
            intent.putExtra(SCREENING, Parcels.wrap(screening));
            intent.putExtra(SHOWTIME, Parcels.wrap(showtime));
            intent.putExtra(SHOWTIME, showtime);
            startActivity(intent);
            finish();

            /* TODO : Go to SELECT SEAT */
        }
    }

    private void reservationRequest(final Screening screening, CheckInRequest checkInRequest, final String showtime) {
        RestClient.getAuthenticated().checkIn(checkInRequest).enqueue(new RestCallback<ReservationResponse>() {
            @Override
            public void onResponse(Call<ReservationResponse> call, Response<ReservationResponse> response) {
                ReservationResponse reservationResponse = response.body();
                if (reservationResponse != null & response.isSuccessful()) {
                    reservation = reservationResponse.getReservation();
                    mProgress.setVisibility(View.GONE);

                    Log.d("screening,", screening.toString());
                    ScreeningToken token = new ScreeningToken(screening, showtime, reservation);

                    if (UserPreferences.getIsVerificationRequired()) {
                        showVerification(token);
                    } else {
                        showConfirmation(token);
                    }
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());

                        //PENDING RESERVATION GO TO TicketConfirmationActivity or TicketVerificationActivity
                        mProgress.setVisibility(View.GONE);
                        mAction.setEnabled(true);
                        Toast.makeText(TheaterActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
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

    private void showConfirmation(ScreeningToken token) {
        mProgress.setVisibility(View.GONE);
        Intent confirmationIntent = new Intent(TheaterActivity.this, ConfirmationActivity.class);
        confirmationIntent.putExtra(TOKEN, Parcels.wrap(token));
        startActivity(confirmationIntent);
        finish();
    }

    private void showVerification(ScreeningToken token) {
        mProgress.setVisibility(View.GONE);
        Intent confirmationIntent = new Intent(TheaterActivity.this, VerificationActivity.class);
        confirmationIntent.putExtra(TOKEN, Parcels.wrap(token));
        startActivity(confirmationIntent);
        finish();
    }

    /* Bottom Navigation Things */

    int getContentViewId() {
        return R.layout.activity_browse;
    }

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
                    startActivity(new Intent(getApplicationContext(), ReservationsActivity.class));
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