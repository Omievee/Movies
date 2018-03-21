package com.mobile.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.mobile.Constants;
import com.mobile.UserLocationManagerFused;
import com.mobile.UserPreferences;
import com.mobile.adapters.MovieTheatersAdapter;
import com.mobile.fragments.SynopsisFragment;
import com.mobile.helpers.BottomNavigationViewHelper;
import com.mobile.helpers.GoWatchItSingleton;
import com.mobile.listeners.ShowtimeClickListener;
import com.mobile.model.Movie;
import com.mobile.model.Reservation;
import com.mobile.model.Screening;
import com.mobile.model.ScreeningToken;
import com.mobile.model.Theater;
import com.mobile.network.RestCallback;
import com.mobile.network.RestClient;
import com.mobile.network.RestError;
import com.mobile.requests.CardActivationRequest;
import com.mobile.requests.CheckInRequest;
import com.mobile.requests.PerformanceInfoRequest;
import com.mobile.requests.TicketInfoRequest;
import com.mobile.responses.CardActivationResponse;
import com.mobile.responses.GoWatchItResponse;
import com.mobile.responses.HistoryResponse;
import com.mobile.responses.ReservationResponse;
import com.mobile.responses.ScreeningsResponse;
import com.moviepass.BuildConfig;
import com.moviepass.R;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.security.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MovieActivity extends BaseActivity implements ShowtimeClickListener {

    public static final String MOVIE = "movie";
    public static final String TITLE = "title";
    public static final String RESERVATION = "reservation";
    public static final String DEEPLINK = "deep_link";
    public static final String THEATER = "theater";
    public static final String SCREENING = "screening";
    public static final String SHOWTIME = "showtime";
    public static final String CAMPAIGN = "campaign";
    public static final String TOKEN = "token";
    private static final String TAG = "TAG";


    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    HistoryResponse historyResponse;
    LocationUpdateBroadCast mLocationBroadCast;
    boolean mLocationAcquired;
    private Location mMyLocation;
    ArrayList<String> mShowtimesList;
    String campaign = "no_campaign";
    String url = "";

    boolean isfirst;

    public Movie movie;
    Reservation reservation;
    protected BottomNavigationView bottomNavigationView;
    MovieTheatersAdapter movieTheatersAdapter;

    ScreeningsResponse screeningsResponse;


    ImageView backButton;
    TextView THEATER_ADDRESS_LISTITEM, noTheaters;
    TextView selectedMovieTitle;
    ImageButton selectedMovieSynopsis;

    ArrayList<Screening> selectedScreeningsList;
    ArrayList<Theater> theatersList;
    ArrayList<Screening> sortedScreeningList;

    ArrayList<String> selectedShowtimesList;

    @BindView(R.id.SELECTED_THEATERS)
    RecyclerView selectedTheatersRecyclerView;

    @BindView(R.id.SELECTED_MOVIE_IMAGE)
    SimpleDraweeView selectedMoviePoster;

    @BindView(R.id.SELECTED_RUNTIME)
    TextView selectedRuntime;

    @BindView(R.id.button_check_in)
    Button buttonCheckIn;

    @BindView(R.id.SELECTED_SYNOPSIS)
    ImageButton selectedSynopsis;

    @BindView(R.id.progress)
    View ProgressBar;

    TextView filmRating;


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
        movie = Parcels.unwrap(getIntent().getParcelableExtra(MOVIE));
        campaign = GoWatchItSingleton.getInstance().getCampaign();
        url = getIntent().getStringExtra(DEEPLINK);


        noTheaters = findViewById(R.id.NoTheaters);
        selectedMoviePoster = findViewById(R.id.SELECTED_MOVIE_IMAGE);
        selectedMovieTitle = findViewById(R.id.SELECTED_MOVIE_TITLE);
        THEATER_ADDRESS_LISTITEM = findViewById(R.id.THEATER_ADDRESS2_LISTITEM);
        selectedRuntime = findViewById(R.id.SELECTED_RUNTIME);
        buttonCheckIn = findViewById(R.id.button_check_in);
        ProgressBar = findViewById(R.id.progress);

        filmRating = findViewById(R.id.SELECTED_FILM_RATING);

        ProgressBar.setVisibility(View.VISIBLE);

        selectedSynopsis = findViewById(R.id.SELECTED_SYNOPSIS);
        mShowtimesList = new ArrayList<>();

        int res2 = R.anim.layout_anim_bottom;
        LayoutAnimationController animation2 = AnimationUtils.loadLayoutAnimation(this, res2);


        UserLocationManagerFused.getLocationInstance(this).startLocationUpdates();
        mLocationBroadCast = new LocationUpdateBroadCast();
        registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));
        currentLocationTasks();

        loadMoviePosterData();
        selectedMovieTitle.setText(movie.getTitle());
        int t = movie.getRunningTime();
        int hours = t / 60;
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
        theatersList = new ArrayList<>();
        sortedScreeningList = new ArrayList<>();


        /* Theaters RecyclerView */
        LinearLayoutManager moviesLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        selectedTheatersRecyclerView = findViewById(R.id.SELECTED_THEATERS);
        selectedTheatersRecyclerView.setLayoutManager(moviesLayoutManager);


        movieTheatersAdapter = new MovieTheatersAdapter(theatersList, sortedScreeningList, this);


        selectedTheatersRecyclerView.setLayoutAnimation(animation2);


        /* Showtimes RecyclerView */
        selectedShowtimesList = new ArrayList<>();

        filmRating.setText("Rated: " + movie.getRating());

        if (url == null || url.isEmpty())
            url = "https://moviepass.com/go/movies/" + movie.getId();
        if (campaign != null && !campaign.isEmpty() && !campaign.equalsIgnoreCase("no_campaign"))
            url = url + "/" + campaign;
        GoWatchItSingleton.getInstance().userOpenedMovie(String.valueOf(movie.getId()), url);

        Log.d(TAG, "onCreate: ");

    }


    @Override
    public void onPause() {
        super.onPause();

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

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public void onShowtimeClick(Theater theater, int pos, final Screening screening, final String showtime) {

        GoWatchItSingleton.getInstance().userClickedOnShowtime(theater, screening, showtime, String.valueOf(movie.getId()), url);
        fadeIn(buttonCheckIn);
        buttonCheckIn.setVisibility(View.VISIBLE);
        buttonCheckIn.setEnabled(true);
        buttonCheckIn.setOnClickListener(view -> {

            if (isPendingSubscription() && screening.getProvider().ticketType.matches("E_TICKET")) {
                ProgressBar.setVisibility(View.VISIBLE);
                reserve(theater, screening, showtime);
            } else if (isPendingSubscription() && screening.getProvider().ticketType.matches("STANDARD")) {
                showActivateCardDialog(screening, showtime);
            } else if (isPendingSubscription() && screening.getProvider().ticketType.matches("SELECT_SEATING")) {
                ProgressBar.setVisibility(View.VISIBLE);
                reserve(theater, screening, showtime);
            } else if (screening.getProvider().ticketType.matches("STANDARD")) {
                if (UserPreferences.getProofOfPurchaseRequired() || screening.isPopRequired()) {
                    alertTicketVerifNotice(theater, screening, showtime);
                } else {
                    ProgressBar.setVisibility(View.VISIBLE);
                    reserve(theater, screening, showtime);
                }
            } else {
                ProgressBar.setVisibility(View.VISIBLE);
                reserve(theater, screening, showtime);
            }
        });
    }


    void alertTicketVerifNotice(Theater theater, Screening screening, String showtime) {
        AlertDialog.Builder alert = new AlertDialog.Builder(MovieActivity.this, R.style.CUSTOM_ALERT);
        alert.setView(R.layout.alertdialog_ticketverif);

        alert.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            ProgressBar.setVisibility(View.VISIBLE);
            reserve(theater, screening, showtime);
        });

        alert.show();
    }

    public void reserve(Theater theater, Screening screening, String showtime) {
        Location mCurrentLocation = UserLocationManagerFused.getLocationInstance(this).mCurrentLocation;
        UserLocationManagerFused.getLocationInstance(this).updateLocation(mCurrentLocation);
        buttonCheckIn.setEnabled(false);

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
            reservationRequest(theater, screening, checkInRequest, showtime);
        } else if (screening.getProvider().ticketType.matches("E_TICKET")) {
            PerformanceInfoRequest performanceInfo = new PerformanceInfoRequest(dateTime, externalMovieId, performanceNumber, tribuneTheaterId, format, normalizedMovieId, sku, price, auditorium, performanceId, sessionId);
            TicketInfoRequest ticketInfo = new TicketInfoRequest(performanceInfo);
            CheckInRequest checkInRequest = new CheckInRequest(ticketInfo, providerName, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            reservationRequest(theater, screening, checkInRequest, showtime);
        } else {

            Intent intent = new Intent(this, SelectSeatActivity.class);
            intent.putExtra(SCREENING, Parcels.wrap(screening));
            intent.putExtra(SHOWTIME, showtime);
            intent.putExtra(MovieActivity.MOVIE, Parcels.wrap(movie));
            intent.putExtra(THEATER, theater);
            startActivity(intent);
            Log.d(TAG, "reserve: " + screening.getProvider().getProviderName());
            finish();
        }

    }

    private void reservationRequest(final Theater theater, final Screening screening, CheckInRequest checkInRequest, final String showtime) {
        RestClient.getAuthenticated().checkIn(checkInRequest).enqueue(new RestCallback<ReservationResponse>() {
            @Override
            public void onResponse(Call<ReservationResponse> call, Response<ReservationResponse> response) {
                ReservationResponse reservationResponse = response.body();
                GoWatchItSingleton.getInstance().checkInEvent(theater, screening, showtime, "ticket_purchase_attempt", String.valueOf(movie.getId()), url);

                if (reservationResponse != null && reservationResponse.isOk()) {
                    reservation = reservationResponse.getReservation();

                    GoWatchItSingleton.getInstance().checkInEvent(theater, screening, showtime, "ticket_purchase", String.valueOf(movie.getId()), url);
                    if (reservationResponse.getE_ticket_confirmation() != null) {
                        String qrUrl = reservationResponse.getE_ticket_confirmation().getBarCodeUrl();
                        String confirmationCode = reservationResponse.getE_ticket_confirmation().getConfirmationCode();

                        ScreeningToken token = new ScreeningToken(screening, showtime, reservation, qrUrl, confirmationCode);
                        showConfirmation(token);
                    } else {

                        ScreeningToken token = new ScreeningToken(screening, showtime, reservation);
                        showConfirmation(token);
                    }
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(MovieActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        Log.d(TAG, "RESPONSE: " + jObjError.getString("messege").toString());
                        ProgressBar.setVisibility(View.GONE);
                        buttonCheckIn.setVisibility(View.VISIBLE);
                        buttonCheckIn.setEnabled(true);
                    } catch (Exception e) {
                        Toast.makeText(MovieActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.d(TAG, "onResponse: " + e.getMessage());
                        ProgressBar.setVisibility(View.GONE);
                        buttonCheckIn.setVisibility(View.VISIBLE);
                        buttonCheckIn.setEnabled(true);
                    }
                }

            }

            @Override
            public void failure(RestError restError) {
                ProgressBar.setVisibility(View.GONE);
                buttonCheckIn.setVisibility(View.VISIBLE);
                buttonCheckIn.setEnabled(true);
                String hostname = "Unable to resolve host: No address associated with hostname";

            }
        });
    }

    private void showConfirmation(ScreeningToken token) {
        Intent confirmationIntent = new Intent(MovieActivity.this, ConfirmationActivity.class);
        confirmationIntent.putExtra(TOKEN, Parcels.wrap(token));
        startActivity(confirmationIntent);
        finish();
    }


    private void loadTheaters(Double latitude, Double longitude, int moviepassId) {
        Log.d(TAG, "MADE IT-------<<<<<: ");
        RestClient.getAuthenticated().getScreeningsForMovie(latitude, longitude, moviepassId)
                .enqueue(new retrofit2.Callback<ScreeningsResponse>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(Call<ScreeningsResponse> call, final Response<ScreeningsResponse> response) {
                        if (response != null && response.isSuccessful()) {
                            screeningsResponse = response.body();
                            //Initial View to Display RecyclerView Based on User's Current Location
                            selectedScreeningsList.clear();
                            theatersList.clear();
                            sortedScreeningList.clear();
                            Collections.sort(theatersList, (theater, t1) -> Double.compare(theater.getDistance(), t1.getDistance()));

                            //Sort Theaters & have screenings follow suit
                            selectedScreeningsList.addAll(screeningsResponse.getScreenings());
                            theatersList.addAll(screeningsResponse.getTheaters());
                            for (int i = 0; i < theatersList.size(); i++) {
                                Theater t = theatersList.get(i);
                                int ID = t.getTribuneTheaterId();
                                for (int j = 0; j < selectedScreeningsList.size(); j++) {
                                    int screenID = selectedScreeningsList.get(j).getTribuneTheaterId();
                                    if (screenID == ID) {
                                        sortedScreeningList.add(selectedScreeningsList.get(j));
                                    }

                                }
                            }

                            if (sortedScreeningList.size() == 0) {
                                selectedTheatersRecyclerView.setVisibility(View.GONE);
                                noTheaters.setVisibility(View.VISIBLE);
                            }


                            if (movieTheatersAdapter != null) {
                                selectedTheatersRecyclerView.getRecycledViewPool().clear();
                                movieTheatersAdapter.notifyDataSetChanged();
                            }
                            selectedTheatersRecyclerView.setAdapter(movieTheatersAdapter);
                            ProgressBar.setVisibility(View.GONE);


                            if (movie.getSynopsis().equals("")) {
                                selectedSynopsis.setVisibility(View.GONE);
                                selectedMoviePoster.setClickable(false);
                            } else {
                                selectedMoviePoster.setClickable(true);

                                selectedSynopsis.setOnClickListener(view -> {
                                    String synopsis = movie.getSynopsis();
                                    String title = movie.getTitle();
                                    Bundle bundle = new Bundle();
                                    bundle.putString(MOVIE, synopsis);
                                    bundle.putString(TITLE, title);

                                    SynopsisFragment fragobj = new SynopsisFragment();
                                    fragobj.setArguments(bundle);
                                    FragmentManager fm = getSupportFragmentManager();
                                    fragobj.show(fm, "fr_dialogfragment_synopsis");
                                });

                                selectedMoviePoster.setOnClickListener(v -> {
                                    String synopsis = movie.getSynopsis();
                                    String title = movie.getTitle();
                                    Bundle bundle = new Bundle();
                                    bundle.putString(MOVIE, synopsis);
                                    bundle.putString(TITLE, title);

                                    SynopsisFragment fragobj = new SynopsisFragment();
                                    fragobj.setArguments(bundle);
                                    FragmentManager fm = getSupportFragmentManager();
                                    fragobj.show(fm, "fr_dialogfragment_synopsis");

                                });
                            }


                        }
                    }


                    @Override
                    public void onFailure(Call<ScreeningsResponse> call, Throwable t) {
                        if (t != null) {
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
        bottomNavigationView.postDelayed(() -> {
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
        }, 0);
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
        final Uri imgUrl = Uri.parse(movie.getLandscapeImageUrl());
        selectedMoviePoster.setImageURI(imgUrl);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);

                        if (imgUrl.toString().contains("updateMovieThumb")) {
                            supportStartPostponedEnterTransition();
                            selectedMoviePoster.setImageResource(R.drawable.film_reel_icon);
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
                        selectedMoviePoster.setImageResource(R.drawable.film_reel_icon);
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
        Log.d(TAG, "currentLocationTasks: ");
        registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));
        UserLocationManagerFused.getLocationInstance(MovieActivity.this).startLocationUpdates();
        mLocationAcquired = false;
        boolean enabled = UserLocationManagerFused.getLocationInstance(MovieActivity.this).isLocationEnabled();
        if (!enabled) {
            Log.d(TAG, "enabled: ");
        } else {
            Log.d(TAG, "else: ");
            Location location = UserLocationManagerFused.getLocationInstance(MovieActivity.this).mCurrentLocation;
            Log.d(TAG, "currentLocationTasks: " + location);
            onLocationChanged(location);
            if (location != null) {
                Log.d(TAG, "location not null: ");
                UserLocationManagerFused.getLocationInstance(this).requestLocationForCoords(location.getLatitude(), location.getLongitude(), MovieActivity.this);
            }
        }
    }

    private void showActivateCardDialog(Screening screening, String showtime) {
        Intent activateCard = new Intent(MovieActivity.this, ActivateMoviePassCard.class);
        activateCard.putExtra(SCREENING, Parcels.wrap(screening));
        activateCard.putExtra(SHOWTIME, showtime);
        startActivity(activateCard);

//        View dialoglayout = getLayoutInflater().inflate(R.layout.dialog_activate_card, null);
//        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(MovieActivity.this);
//        alert.setView(dialoglayout);
//
//        final EditText editText = dialoglayout.findViewById(R.id.activate_card);
//        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
//        InputFilter[] filters = new InputFilter[1];
//        filters[0] = new InputFilter.LengthFilter(4);
//        editText.setFilters(filters);
//
//        alert.setTitle(getString(R.string.dialog_activate_card_header));
//        alert.setMessage(R.string.dialog_activate_card_enter_card_digits);
//        alert.setPositiveButton(android.R.string.ok, (dialog, which) -> {
//            String digits = editText.getText().toString();
//            dialog.dismiss();
//
//            if (digits.length() == 4) {
//                CardActivationRequest request = new CardActivationRequest(digits);
//                ProgressBar.setVisibility(View.VISIBLE);
//                RestClient.getAuthenticated().activateCard(request).enqueue(new Callback<CardActivationResponse>() {
//                    @Override
//                    public void onResponse(Call<CardActivationResponse> call, Response<CardActivationResponse> response) {
//                        CardActivationResponse cardActivationResponse = response.body();
//                        ProgressBar.setVisibility(View.GONE);
//
//                        if (cardActivationResponse != null && response.isSuccessful()) {
//                            String cardActivationResponseMessage = cardActivationResponse.getMessage();
//                            Toast.makeText(MovieActivity.this, R.string.dialog_activate_card_successful, Toast.LENGTH_LONG).show();
//                            reserve(screening, showtime);
//                        } else {
//                            Toast.makeText(MovieActivity.this, R.string.dialog_activate_card_bad_four_digits, Toast.LENGTH_LONG).show();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<CardActivationResponse> call, Throwable t) {
//                        ProgressBar.setVisibility(View.GONE);
//                        showActivateCardDialog(screening, showtime);
//                    }
//                });
//            } else {
//                Toast.makeText(MovieActivity.this, R.string.dialog_activate_card_must_enter_four_digits, Toast.LENGTH_LONG).show();
//            }
//        });
//        alert.setNegativeButton("Activate Later", (dialog, which) -> {
//            Toast.makeText(MovieActivity.this, R.string.dialog_activate_card_must_activate_standard_theater, Toast.LENGTH_LONG).show();
//            dialog.dismiss();
//        });
//        alert.show();
    }


//    public void isFirstTime() {
//        RestClient.getAuthenticated().getReservations().enqueue(new Callback<HistoryResponse>() {
//            @Override
//            public void onResponse(Call<HistoryResponse> call, Response<HistoryResponse> response) {
//                if (response.isSuccessful() && response != null) {
//                    historyResponse = response.body();
//                    if (historyResponse.getReservations().size() == 0) {
//
//                        isfirst = true;
//                    } else {
//                        isfirst = false;
//                    }
//
//                }
//            }
//
//            @Override
//            public void onFailure(Call<HistoryResponse> call, Throwable t) {
//
//            }
//        });
//
//    }

//    public void userOpenedMovie(){
//
//        String l = String.valueOf(UserPreferences.getLatitude());
//        String ln = String.valueOf(UserPreferences.getLongitude());
//        String userId = String.valueOf(UserPreferences.getUserId());
//
//        String versionName = BuildConfig.VERSION_NAME;
//        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
//
//
//        RestClient.getAuthenticatedAPIGoWatchIt().openAppEvent("true","Movie",
//                String.valueOf(movie.getId()),"impression",campaign,"app","android",url,"organic",
//                l,ln,userId,"IDFA", versionCode, versionName).enqueue(new RestCallback<GoWatchItResponse>() {
//            @Override
//            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
//                GoWatchItResponse responseBody = response.body();
////                progress.setVisibility(View.GONE);
//
//                Log.d("HEADER MOVIE -- >", "onResponse: "+responseBody.getFollowUrl());
//            }
//
//            @Override
//            public void failure(RestError restError) {
////                progress.setVisibility(View.GONE);
//               // Toast.makeText(MovieActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
//    }

//    public void userClickedOnShowtime(Theater theater, Screening screening, String showtime) {
//
//        String l = String.valueOf(UserPreferences.getLatitude());
//        String ln = String.valueOf(UserPreferences.getLongitude());
//        String userId = String.valueOf(UserPreferences.getUserId());
//
//        String versionName = BuildConfig.VERSION_NAME;
//        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
//        String tht,thd,tn,thc,thr,thz,tha;
//        tht = showtime.trim();
//        tn = screening.getTheaterName();
//        thc = theater.getCity();
//        thr = theater.getState();
//        thz = theater.getZip();
//        tha = theater.getAddress();
//
//        String result="";
//        thd = "";
//        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
//        try {
//            Date date = format1.parse(screening.getDate());
//            SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//             result = format2.format(date);
//            thd = result;
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        RestClient.getAuthenticatedAPIGoWatchIt().clickOnShowtime("engagement","theater_click",tht,thd,tn,thc,thr,thz,tha,"true","Movie",
//                String.valueOf(movie.getId()),campaign,"app","android",url,"organic",
//                l,ln,userId,"IDFA", versionCode, versionName).enqueue(new RestCallback<GoWatchItResponse>() {
//            @Override
//            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
//                GoWatchItResponse responseBody = response.body();
////                progress.setVisibility(View.GONE);
//
//                Log.d("HEADER MOVIE CLICK -- >", "onResponse: "+responseBody.getFollowUrl());
//            }
//
//            @Override
//            public void failure(RestError restError) {
////                progress.setVisibility(View.GONE);
//               // Toast.makeText(MovieActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
//    }
//
//    public void checkInEvent(Theater theater, Screening screening, String showtime, String engagement) {
//
//        String l = String.valueOf(UserPreferences.getLatitude());
//        String ln = String.valueOf(UserPreferences.getLongitude());
//        String userId = String.valueOf(UserPreferences.getUserId());
//
//        String versionName = BuildConfig.VERSION_NAME;
//        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
//        String tht,thd,tn,thc,thr,thz,tha;
//        tht = showtime.trim();
//        tn = screening.getTheaterName();
//        thc = theater.getCity();
//        thr = theater.getState();
//        thz = theater.getZip();
//        tha = theater.getAddress();
//
//        String result="";
//        thd = "";
//        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
//        try {
//            Date date = format1.parse(screening.getDate());
//            SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//            result = format2.format(date);
//            thd = result;
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        RestClient.getAuthenticatedAPIGoWatchIt().ticketPurchase(engagement,tht,thd,tn,thc,thr,thz,tha,"true","Movie",
//                String.valueOf(movie.getId()),campaign,"app","android",url,"organic",
//                l,ln,userId,"IDFA", versionCode, versionName).enqueue(new RestCallback<GoWatchItResponse>() {
//            @Override
//            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
//                GoWatchItResponse responseBody = response.body();
////                progress.setVisibility(View.GONE);
//
//                Log.d("HEADER MOVIE BUY -- >", "onResponse: "+responseBody.getFollowUrl());
//            }
//
//            @Override
//            public void failure(RestError restError) {
////                progress.setVisibility(View.GONE);
//                //Toast.makeText(MovieActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
//    }


}
