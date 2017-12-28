package com.moviepass.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Animatable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.moviepass.Constants;
import com.moviepass.R;
import com.moviepass.UserLocationManagerFused;
import com.moviepass.adapters.MovieTheatersAdapter;
import com.moviepass.fragments.SynopsisFragment;
import com.moviepass.helpers.BottomNavigationViewHelper;
import com.moviepass.listeners.ShowtimeClickListener;
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

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by anubis on 6/9/17.
 */

public class MovieActivity extends BaseActivity implements ShowtimeClickListener {

    public static final String MOVIE = "movie";
    public static final String TITLE = "title";
    public static final String RESERVATION = "reservation";
    public static final String SCREENING = "screeningObject";
    public static final String SHOWTIME = "showtime";
    public static final String TOKEN = "token";
    private static final String TAG = "TAG";


    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    LocationUpdateBroadCast mLocationBroadCast;
    boolean mLocationAcquired;
    private Location mMyLocation;
    ArrayList<String> mShowtimesList;


    public Movie movie;
    Reservation reservation;
    protected BottomNavigationView bottomNavigationView;
    MovieTheatersAdapter movieTheatersAdapter;
    ScreeningsResponse screeningsResponse;
    Screening Screening;

    ImageView backButton;
    TextView THEATER_ADDRESS_LISTITEM;
    TextView selectedMovieTitle;
    View ProgressBar;
    ImageButton selectedMovieSynopsis;

    ArrayList<Screening> selectedScreeningsList;
    ArrayList<String> selectedShowtimesList;

    @BindView(R.id.SELECTED_THEATERS)
    RecyclerView selectedTheatersRecyclerView;

    @BindView(R.id.SELECTED_MOVIE_IMAGE)
    SimpleDraweeView selectedMoviePoster;

    @BindView(R.id.SELECTED_RUNTIME)
    TextView selectedRuntime;

    @BindView(R.id.FAB_LOADCARD)
    com.github.clans.fab.FloatingActionButton fabLoadCard;

    @BindView(R.id.SELECTED_SYNOPSIS)
    ImageButton selectedSynopsis;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_movie);
        supportPostponeEnterTransition();


        supportStartPostponedEnterTransition();
        supportPostponeEnterTransition();
        final Toolbar mToolbar = findViewById(R.id.SELECTED_TOOLBAR);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        bottomNavigationView = findViewById(R.id.SELECTED_MOVIE_BOTTOMNAV);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        Bundle extras = getIntent().getExtras();
//        extras.getBundle();

        movie = Parcels.unwrap(getIntent().getParcelableExtra(MOVIE));

        selectedMoviePoster = findViewById(R.id.SELECTED_MOVIE_IMAGE);
        selectedMovieTitle = findViewById(R.id.SELECTED_MOVIE_TITLE);
        THEATER_ADDRESS_LISTITEM = findViewById(R.id.THEATER_ADDRESS2_LISTITEM);
        selectedRuntime = findViewById(R.id.SELECTED_RUNTIME);
        fabLoadCard = findViewById(R.id.FAB_LOADCARD);
        fabLoadCard.setImageDrawable(getDrawable(R.drawable.ticketnavwhite));
        fabLoadCard.setColorNormal(getResources().getColor(R.color.gray_dark));
        fabLoadCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MovieActivity.this, "Please select a showtime", Toast.LENGTH_SHORT).show();
            }
        });

        selectedSynopsis = findViewById(R.id.SELECTED_SYNOPSIS);
        mShowtimesList = new ArrayList<>();
        ProgressBar = findViewById(R.id.progress);

        backButton = findViewById(R.id.selected_back);

        //Start location tasks
        UserLocationManagerFused.getLocationInstance(this).startLocationUpdates();
        mLocationBroadCast = new LocationUpdateBroadCast();
        registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));

        currentLocationTasks();
        ProgressBar.setVisibility(View.VISIBLE);


        //FRESCO:
        ButterKnife.bind(this, selectedMoviePoster);

        loadMoviePosterData();
        selectedMovieTitle.setText(movie.getTitle());
        int t = movie.getRunningTime();
        int hours = t / 60; //since both are ints, you get an int
        int minutes = t % 60;

        if (t == 0) {
            selectedRuntime.setVisibility(View.GONE);
        } else if (hours > 1) {
            String translatedRunTime = hours + " hours " + minutes + " minutes";
            selectedRuntime.setText(translatedRunTime);
        } else {
            String translatedRunTime = hours + " hour " + minutes + " minutes";
            selectedRuntime.setText(translatedRunTime);
        }

        selectedScreeningsList = new ArrayList<>();


        /* Theaters RecyclerView */
        LinearLayoutManager moviesLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        selectedTheatersRecyclerView = findViewById(R.id.SELECTED_THEATERS);
        selectedTheatersRecyclerView.setLayoutManager(moviesLayoutManager);
        movieTheatersAdapter = new MovieTheatersAdapter(selectedScreeningsList, this);
        selectedTheatersRecyclerView.setAdapter(movieTheatersAdapter);
        selectedTheatersRecyclerView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        selectedTheatersRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

                        for (int i = 0; i < selectedTheatersRecyclerView.getChildCount(); i++) {
                            View v = selectedTheatersRecyclerView.getChildAt(i);
                            v.setAlpha(0.0f);
                            v.animate().alpha(1.0f)
                                    .setDuration(1000)
                                    .setStartDelay(i * 50)
                                    .start();
                        }

                        return true;
                    }
                });



        /* Showtimes RecyclerView */

        selectedShowtimesList = new ArrayList<>();


        selectedSynopsis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String synopsis = movie.getSynopsis();
                String title = movie.getTitle();
                Bundle bundle = new Bundle();
                bundle.putString(MOVIE, synopsis);
                bundle.putString(TITLE, title);

                SynopsisFragment fragobj = new SynopsisFragment();
                fragobj.setArguments(bundle);
                FragmentManager fm = getSupportFragmentManager();
                fragobj.show(fm, "fr_dialogfragment_synopsis");

                Log.d(TAG, "syno: " + movie.getSynopsis());
            }
        });


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

        if (ProgressBar.getVisibility() == View.VISIBLE) {
            ProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();

    }


    public void onShowtimeClick(int pos, final Screening screening, final String showtime) {
        final String time = showtime;

        fabLoadCard.setColorNormal(getResources().getColor(R.color.new_red));
        fabLoadCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isPendingSubscription()) {

                    showActivateCardDialog(screening, time);

                } else {
                    ProgressBar.setVisibility(View.VISIBLE);
                    reserve(screening, time);
                }
            }
        });
    }

    public void reserve(Screening screening, String showtime) {
        fabLoadCard.setEnabled(false);
        Location mCurrentLocation = UserLocationManagerFused.getLocationInstance(this).mCurrentLocation;
        UserLocationManagerFused.getLocationInstance(this).updateLocation(mCurrentLocation);

        Log.d(TAG, "showtime: " + showtime);
        Log.d(TAG, "provider: " + screening.getProvider());
        Log.d(TAG, "perfominfo: " + screening.getProvider().getPerformanceInfo(showtime));

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
            PerformanceInfoRequest performanceInfo = new PerformanceInfoRequest(dateTime, externalMovieId, performanceNumber, tribuneTheaterId, format, normalizedMovieId, sku, price, auditorium, performanceId, sessionId);
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
                    ProgressBar.setVisibility(View.GONE);

                    if (reservationResponse.getE_ticket_confirmation() != null) {
                        String qrUrl = reservationResponse.getE_ticket_confirmation().getBarCodeUrl();
                        String confirmationCode = reservationResponse.getE_ticket_confirmation().getConfirmationCode();

                        ScreeningToken token = new ScreeningToken(screening, showtime, reservation, qrUrl, confirmationCode);
                        showConfirmation(token);
                    } else {
                        Log.d("screeningObject,", screening.toString());

                        ScreeningToken token = new ScreeningToken(screening, showtime, reservation);
                        showConfirmation(token);
                    }
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(MovieActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        ProgressBar.setVisibility(View.GONE);
                        Log.d(TAG, "try/catch: ");
                        fabLoadCard.setEnabled(true);
                    } catch (Exception e) {
                        Log.d(TAG, "exception: ");
                        Toast.makeText(MovieActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        ProgressBar.setVisibility(View.GONE);
                        fabLoadCard.setEnabled(true);
                    }
                }

            }

            @Override
            public void failure(RestError restError) {
                ProgressBar.setVisibility(View.GONE);
                fabLoadCard.setEnabled(true);

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
        ProgressBar.setVisibility(View.GONE);
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
                                ProgressBar.setVisibility(View.GONE);
                                Toast.makeText(MovieActivity.this, "No Theaters Found", Toast.LENGTH_SHORT).show();
                            } else {

                                //Initial View to Display RecyclerView Based on User's Current Location
                                screeningsResponse = response.body();
                                selectedScreeningsList.clear();
                                if (movieTheatersAdapter != null) {
                                    selectedTheatersRecyclerView.getRecycledViewPool().clear();
                                    movieTheatersAdapter.notifyDataSetChanged();
                                }

                                if (screeningsResponse != null) {
                                    Log.d("getScreenings", screeningsResponse.getScreenings().toString());
                                    selectedScreeningsList.addAll(screeningsResponse.getScreenings());
                                    selectedTheatersRecyclerView.setAdapter(movieTheatersAdapter);
                                    selectedTheatersRecyclerView.getViewTreeObserver().addOnPreDrawListener(
                                            new ViewTreeObserver.OnPreDrawListener() {

                                                @Override
                                                public boolean onPreDraw() {
                                                    selectedTheatersRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                                                    for (int i = 0; i < selectedTheatersRecyclerView.getChildCount(); i++) {
                                                        View v = selectedTheatersRecyclerView.getChildAt(i);
                                                        v.setAlpha(0.0f);
                                                        v.animate().alpha(1.0f)
                                                                .setDuration(1000)
                                                                .setStartDelay(i * 50)
                                                                .start();
                                                    }

                                                    return true;
                                                }
                                            });

                                    ProgressBar.setVisibility(View.GONE);
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


//    /* Bottom Navigation View */

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
//
//    else if (itemId == R.id.action_reservations) {
//        startActivity(new Intent(getApplicationContext(), ReservationsActivity.class));

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
        selectedMoviePoster.setImageURI(imgUrl);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);

                        if (imgUrl.toString().contains("updateMovieThumb")) {
                            supportStartPostponedEnterTransition();
                            selectedMoviePoster.setImageResource(R.drawable.activity_splash_star);
                            selectedMoviePoster.animate();
                            selectedMovieTitle.setText(movie.getTitle());
                        } else {
                            supportStartPostponedEnterTransition();
                            selectedMoviePoster.animate();
                            selectedMoviePoster.setImageURI(imgUrl);
                            selectedMovieTitle.setText(movie.getTitle());
                            selectedMoviePoster.getHierarchy().setFadeDuration(200);
                        }
                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        supportStartPostponedEnterTransition();
                        selectedMoviePoster.setImageResource(R.drawable.activity_splash_star);
                        selectedMovieTitle.setText(movie.getTitle());
                    }
                })
                .setUri(imgUrl)
                .build();
        selectedMoviePoster.setController(controller);
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
                    ProgressBar.setVisibility(View.VISIBLE);

                    RestClient.getAuthenticated().activateCard(request).enqueue(new retrofit2.Callback<CardActivationResponse>() {
                        @Override
                        public void onResponse(Call<CardActivationResponse> call, Response<CardActivationResponse> response) {
                            CardActivationResponse cardActivationResponse = response.body();
                            ProgressBar.setVisibility(View.GONE);

                            if (cardActivationResponse != null && response.isSuccessful()) {
                                String cardActivationResponseMessage = cardActivationResponse.getMessage();
                                Toast.makeText(MovieActivity.this, R.string.dialog_activate_card_successful, Toast.LENGTH_LONG).show();

                                reserve(screening, showtime);
                            } else {
                                Toast.makeText(MovieActivity.this, R.string.dialog_activate_card_bad_four_digits, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<CardActivationResponse> call, Throwable t) {
                            ProgressBar.setVisibility(View.GONE);

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
}
