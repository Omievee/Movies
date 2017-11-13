package com.moviepass.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
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
import android.text.InputFilter;
import android.text.InputType;
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
import android.widget.EditText;
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
import com.moviepass.requests.CardActivationRequest;
import com.moviepass.requests.CheckInRequest;
import com.moviepass.requests.PerformanceInfoRequest;
import com.moviepass.requests.TicketInfoRequest;
import com.moviepass.responses.CardActivationResponse;
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
    public static final String SCREENING = "mScreening";
    public static final String SHOWTIME = "showtime";
    public static final String TOKEN = "token";

    TheaterMoviesAdapter theaterMoviesAdapter;
    TheaterShowtimesAdapter theaterShowtimesAdapter;

    ArrayList<Screening> moviesList;
    ArrayList<String> showtimesList;

    protected BottomNavigationView bottomNavigationView;

    Theater theater;
    ScreeningsResponse screeningsResponse;
    Reservation reservation;
    TextView theaterName;
    TextView theaterAddress;
    TextView theaterCityThings;
    TextView movieTitle;
    TextView movieSelectTime;
    View belowShowtimes;
    View screenBottom;
    Button action;
    View progress;
    View redView;
    Location currentLocation;

    @BindView(R.id.recycler_view_movies)
    RecyclerView moviesRecyclerView;
    @BindView(R.id.recycler_view_showtimes)
    RecyclerView showtimesRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theater);

        final Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();

        // Enable the Up button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        bottomNavigationView = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        Bundle extras = getIntent().getExtras();
        extras.getBundle(TheaterActivity.THEATER);
        theater = Parcels.unwrap(getIntent().getParcelableExtra(THEATER));

        theaterName = findViewById(R.id.theater_name);
        theaterAddress = findViewById(R.id.theater_address);
        theaterCityThings = findViewById(R.id.theater_city_things);
        movieTitle = findViewById(R.id.movie_title);
        movieSelectTime = findViewById(R.id.movie_select_time);
        belowShowtimes = findViewById(R.id.below_showtimes);
        belowShowtimes.setFocusable(true);
        belowShowtimes.setFocusableInTouchMode(true);
        screenBottom = findViewById(R.id.bottom_of_screen);
        screenBottom.setFocusable(true);
        screenBottom.setFocusableInTouchMode(true);
        action = findViewById(R.id.button_action);
        action.setFocusable(true);
        action.setFocusableInTouchMode(true);
        progress = findViewById(R.id.progress);

        theaterName.setText(theater.getName());
        theaterAddress.setText(theater.getAddress());
        String cityThings = (theater.getCity() + " " + theater.getState() + ", " + theater.getZip());
        theaterCityThings.setText(cityThings);

        toolbar.setTitle(theater.getName());
        actionBar.setTitle(theater.getName());

        moviesList = new ArrayList<>();
        showtimesList = new ArrayList<>();

        /* Start Location Tasks */
        UserLocationManagerFused.getLocationInstance(this).startLocationUpdates();

        /* Movies RecyclerView */
        LinearLayoutManager moviesLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        moviesRecyclerView = findViewById(R.id.recycler_view_movies);
        moviesRecyclerView.setLayoutManager(moviesLayoutManager);

        theaterMoviesAdapter = new TheaterMoviesAdapter(moviesList, this);

        moviesRecyclerView.setAdapter(theaterMoviesAdapter);
        moviesRecyclerView.getViewTreeObserver().addOnPreDrawListener(
            new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    moviesRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

                    for (int i = 0; i < moviesRecyclerView.getChildCount(); i++) {
                        View v = moviesRecyclerView.getChildAt(i);
                        v.setAlpha(0.0f);
//                        v.animate().alpha(1.0f)
//                                .setDuration(1000)
//                                .setStartDelay(i * 50)
//                                .start();
                    }

                    return true;
                }
            });

        loadMovies();

        /* Showtimes RecyclerView */

        showtimesRecyclerView = findViewById(R.id.recycler_view_showtimes);
        showtimesRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        redView = findViewById(R.id.red);
        redView.setVisibility(View.INVISIBLE);
        toolbar.setVisibility(View.VISIBLE);

        //TODO: Bring back animations once polished.. Simpler Animations?.. 
//        if (Build.VERSION.SDK_INT >= 21) {
//            redView = findViewById(R.id.red);
//
//            final Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    // previously visible view
//                    // get the center for the clipping circle
//                    int cx = redView.getWidth() / 2;
//                    int cy = redView.getHeight() / 2;
//
//                    Log.d("cx", String.valueOf(cx));
//
//                    // get the initial radius for the clipping circle
//                    float initialRadius = (float) Math.hypot(cx, cy);
//
//                    // create the animation (the final radius is zero)
//                    Animator anim = ViewAnimationUtils.createCircularReveal(redView, cx, cy, initialRadius, 0);
//
//                    // make the view invisible when the animation is done
//                    anim.addListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            super.onAnimationEnd(animation);
//                            redView.setVisibility(View.INVISIBLE);
//                            toolbar.setVisibility(View.VISIBLE);
//                        }
//                    });
//
//                    anim.start();
//                }
//
//            }, 100);
//        }
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
        int theaterId = theater.getTribuneTheaterId();

        RestClient.getAuthenticated().getScreeningsForTheater(theaterId).enqueue(new Callback<ScreeningsResponse>() {
            @Override
            public void onResponse(Call<ScreeningsResponse> call, Response<ScreeningsResponse> response) {
                screeningsResponse = response.body();

                if (screeningsResponse != null) {

                    moviesList.clear();

                    if (moviesRecyclerView != null) {
                        moviesRecyclerView.getRecycledViewPool().clear();
                        theaterMoviesAdapter.notifyDataSetChanged();
                    }

                    moviesList.addAll(screeningsResponse.getScreenings());

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

//        if (Build.VERSION.SDK_INT >= 21) {
//            redView = findViewById(R.id.red);
//
//            final Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    // previously visible view
//                    // get the center for the clipping circle
//                    int cx = redView.getWidth() / 2;
//                    int cy = redView.getHeight() / 2;
//
//                    Log.d("cx", String.valueOf(cx));
//
//                    // get the initial radius for the clipping circle
//                    float initialRadius = (float) Math.hypot(cx, cy);
//
//                    // create the animation (the final radius is zero)
//                    Animator anim = ViewAnimationUtils.createCircularReveal(redView, cx, cy, initialRadius, 0);
//
//                    // make the view invisible when the animation is done
//                    anim.addListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            super.onAnimationEnd(animation);
//                            redView.setVisibility(View.INVISIBLE);
//                        }
//                    });
//
//                    anim.start();
//                }
//            }, 100);
//        }
    }

    public void onScreeningPosterClick(int pos, Screening screening, List<String> startTimes, ImageView sharedImageView) {
        /* TODO : Get the screenings for that movie */

        boolean qualifiersApproved = screening.getQualifiersApproved();
        theaterShowtimesAdapter = new TheaterShowtimesAdapter(this, showtimesList, screening, this, qualifiersApproved);

        showtimesRecyclerView.setAdapter(theaterShowtimesAdapter);
        showtimesRecyclerView.getViewTreeObserver().addOnPreDrawListener(
            new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    showtimesRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

                    for (int i = 0; i < showtimesRecyclerView.getChildCount(); i++) {
                        View v = showtimesRecyclerView.getChildAt(i);
                        v.setAlpha(0.0f);
                        v.animate().alpha(1.0f)
                                .setDuration(1000)
                                .setStartDelay(i * 50)
                                .start();
                    }

                    return true;
                }
            });

        if (movieSelectTime.getVisibility() == View.VISIBLE) {
            fadeOut(movieSelectTime);

            String atWhatTime = getResources().getString(R.string.activity_theater_movie_time);
            movieSelectTime.setText(screening.getTitle() + " " + atWhatTime);
            fadeIn(movieSelectTime);
        } else {
            movieSelectTime.setVisibility(View.VISIBLE);

            String atWhatTime = getResources().getString(R.string.activity_theater_movie_time);
            movieSelectTime.setText(screening.getTitle() + " " + atWhatTime);
            fadeIn(movieSelectTime);
        }

        if (action.getVisibility() == View.VISIBLE) {
            fadeOut(action);
            action.setVisibility(View.INVISIBLE);
        }

        showtimesRecyclerView.setVisibility(View.VISIBLE);
        showtimesRecyclerView.requestFocus();
        showtimesList.clear();

        if (showtimesRecyclerView != null) {
            showtimesRecyclerView.getRecycledViewPool().clear();
            theaterShowtimesAdapter.notifyDataSetChanged();
        }

        belowShowtimes.setVisibility(View.VISIBLE);

        showtimesList.addAll(startTimes);

        belowShowtimes.requestFocus();
    }

    public void onShowtimeClick(int pos, final Screening screening, String showtime) {
        final String time = showtime;

        action.setVisibility(View.VISIBLE);

        if (action.getVisibility() == View.VISIBLE) {
            fadeOut(action);
            fadeIn(action);
            screenBottom.setVisibility(View.VISIBLE);
            screenBottom.requestFocus();
        } else {
            fadeIn(action);
            action.setVisibility(View.VISIBLE);
            screenBottom.setVisibility(View.VISIBLE);
            screenBottom.requestFocus();
        }

        String ticketType = screening.getProvider().ticketType;

        if (ticketType.matches("STANDARD")) {
            String checkIn = "Check In";
            action.setText(checkIn);
        } else if (ticketType.matches("E_TICKET")) {
            String reserve = "Reserve E-Ticket";
            action.setText(reserve);
        } else {
            String selectSeat = "Select Seat";
            action.setText(selectSeat);
        }

        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPendingSubscription()) {
                    showActivateCardDialog(screening, time);
                } else {
                    progress.setVisibility(View.VISIBLE);
                    reserve(screening, time);
                }
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
        action.setEnabled(false);

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
            Log.d("ticketType", screening.getProvider().ticketType);
            Intent intent = new Intent(TheaterActivity.this, SelectSeatActivity.class);
            intent.putExtra(SCREENING, Parcels.wrap(screening));
            intent.putExtra(SHOWTIME, showtime);
            intent.putExtra(THEATER, Parcels.wrap(theater));
            startActivity(intent);
            finish();
        }
    }

    private void reservationRequest(final Screening screening, CheckInRequest checkInRequest, final String showtime) {
        RestClient.getAuthenticated().checkIn(checkInRequest).enqueue(new RestCallback<ReservationResponse>() {
            @Override
            public void onResponse(Call<ReservationResponse> call, Response<ReservationResponse> response) {
                ReservationResponse reservationResponse = response.body();

                if (reservationResponse != null & response.isSuccessful()) {
                    reservation = reservationResponse.getReservation();
                    progress.setVisibility(View.GONE);

                    if (reservationResponse.getE_ticket_confirmation() != null) {
                        String qrUrl = reservationResponse.getE_ticket_confirmation().getBarCodeUrl();
                        String confirmationCode = reservationResponse.getE_ticket_confirmation().getConfirmationCode();

                        ScreeningToken token = new ScreeningToken(screening, showtime, reservation, qrUrl, confirmationCode);
                        showConfirmation(token);
                    } else {
                        Log.d("mScreening,", screening.toString());

                        ScreeningToken token = new ScreeningToken(screening, showtime, reservation);
                        showConfirmation(token);
                    }
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());

                        //PENDING RESERVATION GO TO TicketConfirmationActivity or TicketVerificationActivity
                        progress.setVisibility(View.GONE);
                        action.setEnabled(true);
                        Toast.makeText(TheaterActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(TheaterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    Log.d("resResponse:", "else onResponse:" + "onRespnse fail");
                    progress.setVisibility(View.GONE);
                    action.setEnabled(true);
                }

                action.setEnabled(true);
            }

            @Override
            public void failure(RestError restError) {
                progress.setVisibility(View.GONE);
                action.setEnabled(true);

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
        progress.setVisibility(View.GONE);
        Intent confirmationIntent = new Intent(TheaterActivity.this, ConfirmationActivity.class);
        confirmationIntent.putExtra(TOKEN, Parcels.wrap(token));
        startActivity(confirmationIntent);
        finish();
    }

    private void showVerification(ScreeningToken token) {
        progress.setVisibility(View.GONE);
        Intent confirmationIntent = new Intent(TheaterActivity.this, VerificationActivity.class);
        confirmationIntent.putExtra(TOKEN, Parcels.wrap(token));
        startActivity(confirmationIntent);
        finish();
    }

    private void showActivateCardDialog(final Screening screening, final String showtime) {
        View dialoglayout = getLayoutInflater().inflate(R.layout.dialog_activate_card, null);
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(TheaterActivity.this);
        alert.setView(dialoglayout);

        final EditText editText = dialoglayout.findViewById(R.id.activate_card);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(4);
        editText.setFilters(filters);

        alert.setTitle(getString(R.string.dialog_activate_card_header));
        alert.setMessage(R.string.dialog_activate_card_enter_card_digits);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                String digits = editText.getText().toString();
                dialog.dismiss();

                if (digits.length() == 4) {
                    CardActivationRequest request = new CardActivationRequest(digits);
                    progress.setVisibility(View.VISIBLE);

                    RestClient.getAuthenticated().activateCard(request).enqueue(new retrofit2.Callback<CardActivationResponse>() {
                        @Override
                        public void onResponse(Call<CardActivationResponse> call, Response<CardActivationResponse> response) {
                            CardActivationResponse cardActivationResponse = response.body();
                            progress.setVisibility(View.GONE);

                            if (cardActivationResponse != null && response.isSuccessful()) {
                                String cardActivationResponseMessage = cardActivationResponse.getMessage();
                                Toast.makeText(TheaterActivity.this, R.string.dialog_activate_card_successful, Toast.LENGTH_LONG).show();
                                reserve(screening, showtime);
                            } else {
                                Toast.makeText(TheaterActivity.this, R.string.dialog_activate_card_bad_four_digits, Toast.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        public void onFailure(Call<CardActivationResponse> call, Throwable t) {
                            progress.setVisibility(View.GONE);

                            showActivateCardDialog(screening, showtime);
                        }
                    });
                } else {
                    Toast.makeText(TheaterActivity.this, R.string.dialog_activate_card_must_enter_four_digits, Toast.LENGTH_LONG).show();
                }
            }
        });
        alert.setNegativeButton("Activate Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                Toast.makeText(TheaterActivity.this, R.string.dialog_activate_card_must_activate_standard_theater, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        alert.show();
    }

    /* Bottom Navigation Things */

    int getContentViewId() {
        return R.layout.activity_browse;
    }

    int getNavigationMenuItemId() {
        return R.id.action_theaters;
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
                    startActivity(new Intent(getApplicationContext(), ReservationsActivity.class));
                } else if (itemId == R.id.action_movies) {
                    startActivity(new Intent(getApplicationContext(), MoviesActivity.class));
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