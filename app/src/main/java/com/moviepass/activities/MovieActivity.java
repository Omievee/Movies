package com.moviepass.activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v13.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.moviepass.Constants;
import com.moviepass.listeners.MovieTheaterClickListener;
import com.moviepass.R;
import com.moviepass.listeners.ShowtimeClickListener;
import com.moviepass.UserLocationManagerFused;
import com.moviepass.adapters.MovieShowtimesAdapter;
import com.moviepass.adapters.MovieTheatersAdapter;
import com.moviepass.fragments.MoviesFragment;
import com.moviepass.helpers.BottomNavigationViewHelper;
import com.moviepass.model.Movie;
import com.moviepass.model.Reservation;
import com.moviepass.model.Screening;
import com.moviepass.model.ScreeningToken;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by anubis on 6/9/17.
 */

public class MovieActivity extends BaseActivity implements MovieTheaterClickListener, ShowtimeClickListener {

    public static final String MOVIE = "movie";
    public static final String RESERVATION = "reservation";
    public static final String SCREENING = "screening";
    public static final String SHOWTIME = "showtime";
    public static final String TOKEN = "token";
    private static final String TAG = "TAG";

    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    LocationUpdateBroadCast mLocationBroadCast;
    boolean mLocationAcquired;
    private Location mMyLocation;

    Movie movie;
    Reservation reservation;
    protected BottomNavigationView bottomNavigationView;
    MovieTheatersAdapter mMovieTheatersAdapter;
    MovieShowtimesAdapter mMovieShowtimesAdapter;
    ScreeningsResponse mScreeningsResponse;
    Screening mScreening;

    SimpleDraweeView mSelectedPosterImage;
    TextView mSelectedPosterTitle;
    TextView theaterAddress;
    TextView mTitle;
    TextView mGenre;
    TextView mRunTime;
    TextView mTheatersNearby;
    TextView mTheaterSelectTime;
    Button mAction;
    View mBelowShowtimes;
    View mScreenBottom;
    View progress;

    ArrayList<Screening> mScreeningsList;
    ArrayList<String> mShowtimesList;

    @BindView(R.id.recycler_view_theaters)
    RecyclerView theatersRecyclerView;
    @BindView(R.id.recycler_view_showtimes)
    RecyclerView mShowtimesRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        supportPostponeEnterTransition();
        Fresco.initialize(this);

        supportStartPostponedEnterTransition();
        supportPostponeEnterTransition();

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
        movie = Parcels.unwrap(getIntent().getParcelableExtra(MOVIE));

        mSelectedPosterImage = findViewById(R.id.movieSelected_Poster);
        mSelectedPosterTitle = findViewById(R.id.poster_movie_title);
        mTitle = findViewById(R.id.movie_title);
        theaterAddress = findViewById(R.id.theater_address);
        mRunTime = findViewById(R.id.text_run_time);
        mTheaterSelectTime = findViewById(R.id.theater_select_time);
        mTheatersNearby = findViewById(R.id.movie_select_text);
        mAction = findViewById(R.id.button_action);
        mBelowShowtimes = findViewById(R.id.below_showtimes);
        mBelowShowtimes.setFocusable(true);
        mBelowShowtimes.setFocusableInTouchMode(true);
        mScreenBottom = findViewById(R.id.bottom_of_screen);
        mScreenBottom.setFocusable(true);
        mScreenBottom.setFocusableInTouchMode(true);
        progress = findViewById(R.id.progress);

        mToolbar.setTitle(movie.getTitle());
        mActionBar.setTitle(movie.getTitle());

        theaterAddress.setVisibility(View.GONE);

        //Start location tasks
        UserLocationManagerFused.getLocationInstance(this).startLocationUpdates();
        mLocationBroadCast = new LocationUpdateBroadCast();
        registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));

        currentLocationTasks();
        progress.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String imageTransitionName = extras.getString(MoviesFragment.EXTRA_MOVIE_IMAGE_TRANSITION_NAME);
            mSelectedPosterImage.setTransitionName(imageTransitionName);
        }

        //FRESCO:
        loadMoviePosterData();

        mTitle.setText(movie.getTitle());
        int t = movie.getRunningTime();
        int hours = t / 60; //since both are ints, you get an int
        int minutes = t % 60;

        if (t == 0) {
            mRunTime.setVisibility(View.GONE);
        } else if (hours > 1) {
            String translatedRunTime = hours + " hours " + minutes + " minutes";
            mRunTime.setText(translatedRunTime);
        } else {
            String translatedRunTime = hours + " hour " + minutes + " minutes";
            mRunTime.setText(translatedRunTime);
        }

        mScreeningsList = new ArrayList<>();

        /* Theaters RecyclerView */
        LinearLayoutManager moviesLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        theatersRecyclerView = findViewById(R.id.recycler_view_theaters);
        theatersRecyclerView.setLayoutManager(moviesLayoutManager);

        mMovieTheatersAdapter = new MovieTheatersAdapter(mScreeningsList, this);

        theatersRecyclerView.setAdapter(mMovieTheatersAdapter);
        theatersRecyclerView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        theatersRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

                        for (int i = 0; i < theatersRecyclerView.getChildCount(); i++) {
                            View v = theatersRecyclerView.getChildAt(i);
                            v.setAlpha(0.0f);
                            v.animate().alpha(1.0f)
                                    .setDuration(1000)
                                    .setStartDelay(i * 50)
                                    .start();
                        }

                        return true;
                    }
                });

        fadeIn(mTheatersNearby);

        /* Showtimes RecyclerView */
        mShowtimesList = new ArrayList<>();
        mShowtimesRecyclerView = findViewById(R.id.recycler_view_showtimes);
        mShowtimesRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
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
            UserLocationManagerFused.getLocationInstance(MovieActivity.this).stopLocationUpdates();
            onLocationChanged(UserLocationManagerFused.getLocationInstance(context).mCurrentLocation);
        }
    }

    protected void onLocationChanged(Location location) {
        UserLocationManagerFused.getLocationInstance(this).stopLocationUpdates();

        if (location != null) {
            UserLocationManagerFused.getLocationInstance(this).updateLocation(location);

            mMyLocation = location;

            loadTheaters(mMyLocation.getLatitude(), mMyLocation.getLongitude(), movie.getId());

            mLocationAcquired = true;
        }
    }

    public void currentLocationTasks() {
        registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));
        UserLocationManagerFused.getLocationInstance(MovieActivity.this).startLocationUpdates();
        mLocationAcquired = false;
        boolean enabled = UserLocationManagerFused.getLocationInstance(MovieActivity.this).isLocationEnabled();
        if (!enabled) {
//            showDialogGPS();
        } else {
            Location location = UserLocationManagerFused.getLocationInstance(MovieActivity.this).mCurrentLocation;
            onLocationChanged(location);

            if (location != null) {
                UserLocationManagerFused.getLocationInstance(this).requestLocationForCoords(location.getLatitude(), location.getLongitude(), MovieActivity.this);
            }
        }
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);

        try {
            unregisterReceiver(mLocationBroadCast);
        } catch (IllegalArgumentException is) {
            is.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));
        } catch (IllegalArgumentException is) {
            is.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));
        } catch (IllegalArgumentException is) {
            is.printStackTrace();
        }

        if (progress.getVisibility() == View.VISIBLE) {
            progress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();

    }

    public void onTheaterClick(int pos, Screening screening) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        boolean qualifiersApproved = screening.getQualifiersApproved();

        mMovieShowtimesAdapter = new MovieShowtimesAdapter(this, mShowtimesList, screening, this, width, qualifiersApproved);
        mShowtimesRecyclerView.setAdapter(mMovieShowtimesAdapter);

        if (mTheaterSelectTime.getVisibility() != View.VISIBLE) {
            String atWhatTime = getResources().getString(R.string.activity_theater_movie_time);
            mTheaterSelectTime.setText(screening.getTitle() + " " + atWhatTime);
            mTheaterSelectTime.setVisibility(View.VISIBLE);
            fadeIn(mTheaterSelectTime);
        } else {
            fadeOut(mTheaterSelectTime);
            String atWhatTime = getResources().getString(R.string.activity_theater_movie_time);
            mTheaterSelectTime.setText(screening.getTitle() + " " + atWhatTime);
            fadeIn(mTheaterSelectTime);
        }

        if (mAction.getVisibility() == View.VISIBLE) {
            fadeOut(mAction);
            mAction.setVisibility(View.GONE);
        }

        if (mShowtimesRecyclerView.getVisibility() != View.VISIBLE) {
            mShowtimesRecyclerView.setVisibility(View.VISIBLE);
            fadeIn(mShowtimesRecyclerView);
        } else {
            fadeOut(mShowtimesRecyclerView);
            fadeIn(mShowtimesRecyclerView);
        }

        mBelowShowtimes.setVisibility(View.VISIBLE);

        ArrayList<String> startTimes = new ArrayList<>(screening.getStartTimes());

        mShowtimesList.clear();

        if (mShowtimesRecyclerView != null) {
            mShowtimesRecyclerView.getRecycledViewPool().clear();
            mMovieShowtimesAdapter.notifyDataSetChanged();
        }

        mShowtimesList.addAll(startTimes);

        mBelowShowtimes.requestFocus();
    }

    public void onShowtimeClick(int pos, final Screening screening, String showtime) {
        final String time = showtime;
        Animation animShow = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        if (mAction.getVisibility() != View.VISIBLE) {
            mAction.setVisibility(View.VISIBLE);
            fadeIn(mAction);
            mScreenBottom.setVisibility(View.VISIBLE);
            mScreenBottom.requestFocus();
        } else {
            fadeOut(mAction);
            mAction.setVisibility(View.INVISIBLE);
            mAction.setVisibility(View.VISIBLE);
            fadeIn(mAction);
            mScreenBottom.setVisibility(View.VISIBLE);
            mScreenBottom.requestFocus();
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
                if (isPendingSubscription()) {
                    showActivateCardDialog(screening, time);
                } else {
                    progress.setVisibility(View.VISIBLE);
                    reserve(screening, time);
                }
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
        String auditorium = screening.getProvider().getPerformanceInfo(showtime).getAuditorium();
        String performanceId = screening.getProvider().getPerformanceInfo(showtime).getPerformanceId();
        String sessionId = screening.getProvider().getPerformanceInfo(showtime).getSessionId();
        int performanceNumber = screening.getProvider().getPerformanceInfo(showtime).getPerformanceNumber();
        String sku = screening.getProvider().getPerformanceInfo(showtime).getSku();
        Double price = screening.getProvider().getPerformanceInfo(showtime).getPrice();

        if (screening.getProvider().ticketType.matches("STANDARD")) {
            PerformanceInfoRequest performanceInfo = new PerformanceInfoRequest(normalizedMovieId, externalMovieId, format, tribuneTheaterId, screeningId, dateTime);
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
            /* TODO : Go to SELECT SEAT */
            Intent intent = new Intent(this, SelectSeatActivity.class);
            intent.putExtra(SCREENING, Parcels.wrap(screening));
            intent.putExtra(SHOWTIME, showtime);
            intent.putExtra(MovieActivity.MOVIE, Parcels.wrap(movie));
            startActivity(intent);
            finish();
        }

    }

    private void reservationRequest(final Screening screening, CheckInRequest checkInRequest, final String showtime) {
        RestClient.getAuthenticated().checkIn(checkInRequest).enqueue(new RestCallback<ReservationResponse>() {
            @Override
            public void onResponse(Call<ReservationResponse> call, Response<ReservationResponse> response) {
                ReservationResponse reservationResponse = response.body();

                if (reservationResponse != null && reservationResponse.isOk()) {
                    reservation = reservationResponse.getReservation();
                    progress.setVisibility(View.GONE);

                    if (reservationResponse.getE_ticket_confirmation() != null) {
                        String qrUrl = reservationResponse.getE_ticket_confirmation().getBarCodeUrl();
                        String confirmationCode = reservationResponse.getE_ticket_confirmation().getConfirmationCode();

                        ScreeningToken token = new ScreeningToken(screening, showtime, reservation, qrUrl, confirmationCode);
                        showConfirmation(token);
                    } else {
                        Log.d("screening,", screening.toString());

                        ScreeningToken token = new ScreeningToken(screening, showtime, reservation);
                        showConfirmation(token);
                    }
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());

                        Toast.makeText(MovieActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        progress.setVisibility(View.GONE);
                        mAction.setEnabled(true);
                    } catch (Exception e) {
                        Toast.makeText(MovieActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        progress.setVisibility(View.GONE);
                        mAction.setEnabled(true);
                    }
                }

            }

            @Override
            public void failure(RestError restError) {
                progress.setVisibility(View.GONE);
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
        Intent confirmationIntent = new Intent(MovieActivity.this, ConfirmationActivity.class);
        confirmationIntent.putExtra(TOKEN, Parcels.wrap(token));
        startActivity(confirmationIntent);
        finish();
    }

    private void showVerification(ScreeningToken token) {
        progress.setVisibility(View.GONE);
        Intent confirmationIntent = new Intent(MovieActivity.this, VerificationActivity.class);
        confirmationIntent.putExtra(TOKEN, Parcels.wrap(token));
        startActivity(confirmationIntent);
        finish();
    }

    private void loadTheaters(Double latitude, Double longitude, int moviepassId) {
        RestClient.getAuthenticated().getScreeningsForMovie(latitude, longitude, moviepassId)
                .enqueue(new retrofit2.Callback<ScreeningsResponse>() {
                    @Override
                    public void onResponse(Call<ScreeningsResponse> call, final Response<ScreeningsResponse> response) {
                        ScreeningsResponse screenings = response.body();
                        if (screenings != null) {
                            List<Screening> screeningsList = screenings.getScreenings();

                            if (screeningsList.size() == 0) {
                                progress.setVisibility(View.GONE);
                                AlertDialog.Builder builder = new AlertDialog.Builder(MovieActivity.this, R.style.AlertDialogCustom);

/*                            builder.setTitle(R.string.activity_location_no_theaters_found);
                        builder.setMessage(R.string.activity_location_try_another_zip_code);
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }); */

                                builder.show();
                            } else {

                                //Initial View to Display RecyclerView Based on User's Current Location
                                mScreeningsResponse = response.body();
                                mScreeningsList.clear();

                                if (mMovieTheatersAdapter != null) {
                                    theatersRecyclerView.getRecycledViewPool().clear();
                                    mMovieTheatersAdapter.notifyDataSetChanged();
                                }

                                if (mScreeningsResponse != null) {
                                    Log.d("getScreenings", mScreeningsResponse.getScreenings().toString());
                                    mScreeningsList.addAll(mScreeningsResponse.getScreenings());
                                    theatersRecyclerView.setAdapter(mMovieTheatersAdapter);
                                    theatersRecyclerView.getViewTreeObserver().addOnPreDrawListener(
                                            new ViewTreeObserver.OnPreDrawListener() {

                                                @Override
                                                public boolean onPreDraw() {
                                                    theatersRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

                                                    for (int i = 0; i < theatersRecyclerView.getChildCount(); i++) {
                                                        View v = theatersRecyclerView.getChildAt(i);
                                                        v.setAlpha(0.0f);
                                                        v.animate().alpha(1.0f)
                                                                .setDuration(1000)
                                                                .setStartDelay(i * 50)
                                                                .start();
                                                    }

                                                    return true;
                                                }
                                            });

                                    progress.setVisibility(View.GONE);
                                }

                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ScreeningsResponse> call, Throwable t) {
                        if (t != null) {
                            Log.d("Unable to get theaters", "Unable to download theaters: " + t.getMessage());
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

    private void showActivateCardDialog(final Screening screening, final String showtime) {
        View dialoglayout = getLayoutInflater().inflate(R.layout.dialog_activate_card, null);
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(MovieActivity.this);
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
                                Toast.makeText(MovieActivity.this, R.string.dialog_activate_card_successful, Toast.LENGTH_LONG).show();
                                ;
                                reserve(screening, showtime);
                            } else {
                                Toast.makeText(MovieActivity.this, R.string.dialog_activate_card_bad_four_digits, Toast.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        public void onFailure(Call<CardActivationResponse> call, Throwable t) {
                            progress.setVisibility(View.GONE);

                            showActivateCardDialog(screening, showtime);
                        }
                    });
                } else {
                    Toast.makeText(MovieActivity.this, R.string.dialog_activate_card_must_enter_four_digits, Toast.LENGTH_LONG).show();
                }
            }
        });
        alert.setNegativeButton("Activate Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                Toast.makeText(MovieActivity.this, R.string.dialog_activate_card_must_activate_standard_theater, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
//                        updateUI();
                        break;
                }
                break;
        }
    }

    /* PERMISSIONS */

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
/*            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MovieActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    }); */
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MovieActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
/*                if (mRequestingLocationUpdates) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates");
                } */
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
/*                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }); */
            }
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
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
                    startActivity(new Intent(getApplicationContext(), ReservationsActivity.class));
                } else if (itemId == R.id.action_movies) {
                } else if (itemId == R.id.action_theaters) {
                    startActivity(new Intent(getApplicationContext(), TheatersActivity.class));
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

    private void loadMoviePosterData() {
        final Uri imgUrl = Uri.parse(movie.getImageUrl());
        mSelectedPosterImage.setImageURI(imgUrl);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);

                        if (imgUrl.toString().contains("updateMovieThumb")) {
                            supportStartPostponedEnterTransition();
                            mSelectedPosterImage.setImageResource(R.drawable.activity_splash_star);
                            mSelectedPosterTitle.setText(movie.getTitle());
                        } else {
                            supportStartPostponedEnterTransition();
                            mSelectedPosterImage.getHierarchy().setFadeDuration(500);
                        }
                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        supportStartPostponedEnterTransition();
                        mSelectedPosterImage.setImageResource(R.drawable.activity_splash_star);
                        mSelectedPosterTitle.setText(movie.getTitle());
                    }
                })
                .setUri(imgUrl)
                .build();
        mSelectedPosterImage.setController(controller);
    }

}