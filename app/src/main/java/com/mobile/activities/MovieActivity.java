package com.mobile.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Animatable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
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
import com.mobile.helpers.LogUtils;
import com.mobile.listeners.ShowtimeClickListener;
import com.mobile.model.Movie;
import com.mobile.model.Reservation;
import com.mobile.model.Screening;
import com.mobile.model.ScreeningToken;
import com.mobile.model.Theater;
import com.mobile.network.RestCallback;
import com.mobile.network.RestClient;
import com.mobile.network.RestError;
import com.mobile.requests.CheckInRequest;
import com.mobile.requests.PerformanceInfoRequest;
import com.mobile.requests.TicketInfoRequest;
import com.mobile.responses.ReservationResponse;
import com.mobile.responses.ScreeningsResponse;
import com.moviepass.R;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;

import butterknife.BindView;
import io.realm.Realm;
import retrofit2.Call;
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

    Realm historyRealm;
    LocationUpdateBroadCast mLocationBroadCast;
    boolean mLocationAcquired;
    private Location mMyLocation;
    ArrayList<String> mShowtimesList;
    String campaign = "no_campaign";
    String url = "";

    public Movie movie;
    Reservation reservation;
    protected BottomNavigationView bottomNavigationView;
    MovieTheatersAdapter movieTheatersAdapter;

    ScreeningsResponse screeningsResponse;
    View allScreenings;
    View comingSoon;


    TextView THEATER_ADDRESS_LISTITEM, noTheaters, enableLocation, locationMsg;
    ImageView arrow;
    TextView selectedMovieTitle;

    LinkedList<Screening> selectedScreeningsList;
    LinkedList<Theater> theatersList;
    LinkedList<Screening> sortedScreeningList;

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
    private TextView comingSoonTitle, synopsisTitle, synopsisContent;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_movie);
//        supportPostponeEnterTransition();


//        supportStartPostponedEnterTransition();
//        supportPostponeEnterTransition();
        final Toolbar mToolbar = findViewById(R.id.SELECTED_TOOLBAR);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        bottomNavigationView = findViewById(R.id.SELECTED_MOVIE_BOTTOMNAV);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        movie = Parcels.unwrap(getIntent().getParcelableExtra(MOVIE));
        campaign = GoWatchItSingleton.getInstance().getCampaign();
        url = getIntent().getStringExtra(DEEPLINK);


        arrow = findViewById(R.id.arrow);
        enableLocation = findViewById(R.id.EnableText);
        locationMsg = findViewById(R.id.message);
        noTheaters = findViewById(R.id.NoTheaters);
        selectedMoviePoster = findViewById(R.id.SELECTED_MOVIE_IMAGE);
        selectedMovieTitle = findViewById(R.id.SELECTED_MOVIE_TITLE);
        THEATER_ADDRESS_LISTITEM = findViewById(R.id.THEATER_ADDRESS2_LISTITEM);
        selectedRuntime = findViewById(R.id.SELECTED_RUNTIME);
        buttonCheckIn = findViewById(R.id.button_check_in);
        ProgressBar = findViewById(R.id.progress);

        allScreenings = findViewById(R.id.scrennings);
        comingSoon = findViewById(R.id.comingSoon);

        filmRating = findViewById(R.id.SELECTED_FILM_RATING);



        selectedSynopsis = findViewById(R.id.SELECTED_SYNOPSIS);
        mShowtimesList = new ArrayList<>();

        comingSoonTitle = findViewById(R.id.comingSoonTitle);
        synopsisTitle = findViewById(R.id.synopsisTitle);
        synopsisContent = findViewById(R.id.synopsisContent);


        int res2 = R.anim.layout_anim_bottom;
        LayoutAnimationController animation2 = AnimationUtils.loadLayoutAnimation(this, res2);



        // registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));

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


        Date today = Calendar.getInstance().getTime();



        if(movie.getReleaseDate()!=null){
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
            try {
                Date date = format1.parse(movie.getReleaseDate());
                SimpleDateFormat format2 = new SimpleDateFormat("MMM dd, yyyy");
                String result = format2.format(date);
                if(date.before(today)){
                   setShowings();
                } else{
                    selectedSynopsis.setVisibility(View.GONE);
                    selectedMoviePoster.setClickable(false);
                    allScreenings.setVisibility(View.GONE);
                    comingSoon.setVisibility(View.VISIBLE);
                    comingSoonTitle.setText("In Theaters "+result);
                    synopsisContent.setText(movie.getSynopsis());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }else{
            setShowings();
        }



        filmRating.setText("Rated: " + movie.getRating());

        if (url == null || url.isEmpty())
            url = "https://moviepass.com/go/movies/" + movie.getId();
        if (campaign != null && !campaign.isEmpty() && !campaign.equalsIgnoreCase("no_campaign"))
            url = url + "/" + campaign;
        GoWatchItSingleton.getInstance().userOpenedMovie(String.valueOf(movie.getId()), url);


        LogUtils.newLog(TAG, "Selected movie id: " + movie.getId());
    }

    public void setShowings(){
        ProgressBar.setVisibility(View.VISIBLE);
        allScreenings.setVisibility(View.VISIBLE);
        comingSoon.setVisibility(View.GONE);
        selectedScreeningsList = new LinkedList<>();
        theatersList = new LinkedList<>();
        sortedScreeningList = new LinkedList<>();

        UserLocationManagerFused.getLocationInstance(this).startLocationUpdates();
        mLocationBroadCast = new LocationUpdateBroadCast();
        /* Theaters RecyclerView */
        LinearLayoutManager moviesLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        selectedTheatersRecyclerView = findViewById(R.id.SELECTED_THEATERS);
        selectedTheatersRecyclerView.setLayoutManager(moviesLayoutManager);


        movieTheatersAdapter = new MovieTheatersAdapter(theatersList, sortedScreeningList, this);


//        selectedTheatersRecyclerView.setLayoutAnimation(animation2);
        currentLocationTasks();

        /* Showtimes RecyclerView */
        selectedShowtimesList = new ArrayList<>();
    }


    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(movie.getReleaseDate()==null)
            currentLocationTasks();

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public void onShowtimeClick(Theater theater, int pos, final Screening screening, final String showtime) {


        LogUtils.newLog(TAG, "onShowtimeClick: " + UserPreferences.getRestrictionHasActiveCard());
        GoWatchItSingleton.getInstance().userClickedOnShowtime(theater, screening, showtime, String.valueOf(movie.getId()), url);
        if (buttonCheckIn.getVisibility() == View.GONE) {
            fadeIn(buttonCheckIn);
            buttonCheckIn.setVisibility(View.VISIBLE);
        }

        if (screening.getProvider().ticketTypeIsETicket() || screening.getProvider().ticketTypeIsSelectSeating()) {
            buttonCheckIn.setText("Continue to E-Ticketing");
        } else {
            buttonCheckIn.setText("Check In");
        }
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
                    UserPreferences.saveReservation(reservation);
                    GoWatchItSingleton.getInstance().checkInEvent(theater, screening, showtime, "ticket_purchase", String.valueOf(movie.getId()), url);
                    if (reservationResponse.getE_ticket_confirmation() != null) {
                        ScreeningToken token = new ScreeningToken(screening, showtime, reservation, reservationResponse.getE_ticket_confirmation(), theater);
                        showConfirmation(token);
                    } else {
                        ScreeningToken token = new ScreeningToken(screening, showtime, reservation, theater);
                        showConfirmation(token);
                    }
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(MovieActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        LogUtils.newLog(TAG, ":---------------------<<<<<<<<<<<<<<<>>>>>>>>>>>>>> " + jObjError.getString("messege").toString());
                        ProgressBar.setVisibility(View.GONE);
                        buttonCheckIn.setVisibility(View.VISIBLE);
                        buttonCheckIn.setEnabled(true);
                    } catch (Exception e) {
                        LogUtils.newLog(TAG, "onResponse: " + e.getMessage());
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
        RestClient.getAuthenticated().getScreeningsForMovie(latitude, longitude, moviepassId).enqueue(new retrofit2.Callback<ScreeningsResponse>() {

            @Override
            public void onResponse(Call<ScreeningsResponse> call, final Response<ScreeningsResponse> response) {
                if (response != null && response.isSuccessful()) {
                    screeningsResponse = response.body();
                    //Initial View to Display RecyclerView Based on User's Current Location
                    selectedScreeningsList.clear();
                    theatersList.clear();
                    sortedScreeningList.clear();
                    Collections.sort(theatersList, (theater, t1) -> Double.compare(theater.getDistance(), t1.getDistance()));

                    LinkedList<Screening> resorted = new LinkedList<>();
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
                            Screening etix = screeningsResponse.getScreenings().get(j);
                            if (etix.getProvider().ticketTypeIsETicket() || etix.getProvider().ticketTypeIsSelectSeating()) {
                                sortedScreeningList.remove(screeningsResponse.getScreenings().get(j));
                                sortedScreeningList.add(0, screeningsResponse.getScreenings().get(j));
                            }
                        }
                    }

                    int i = 0;
                    int count = sortedScreeningList.size();
                    int currentShowTimes = 0;
                    while (i < sortedScreeningList.size() && count >= 0) {
                        Screening currentScreening = sortedScreeningList.get(i);
                        currentShowTimes = currentScreening.getStartTimes().size();
                        if (currentScreening.getStartTimes() != null) {

                            for (int j = 0; j < currentScreening.getStartTimes().size(); j++) {

                                try {
                                    Date systemClock = new Date();

                                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
                                    String curTime = sdf.format(systemClock);

                                    Date theaterTime = sdf.parse(currentScreening.getStartTimes().get(j));
                                    Date myTime = sdf.parse(curTime);

                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(theaterTime);
                                    cal.add(Calendar.MINUTE, 30);


                                    if (myTime.after(cal.getTime())) {
                                        if (cal.getTime().getHours() > 3) {
                                            currentShowTimes--;
                                        }
                                    }

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (currentShowTimes == 0) {
                                sortedScreeningList.remove(i);
                                i--;
                            } else {
                                Screening notApproved = sortedScreeningList.get(i);
                                if (!notApproved.isApproved()) {
                                    sortedScreeningList.remove(i);
                                    sortedScreeningList.addLast(notApproved);
                                    i--;
                                }
                            }
                            count--;
                        }
                        i++;
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
        android.util.Log.d(TAG, "loadMoviePosterData: " + movie.getLandscapeImageUrl());

        try {
            final Uri imgUrl = Uri.parse(movie.getLandscapeImageUrl());
            selectedMoviePoster.setImageURI(imgUrl);
            DraweeController controller = Fresco
                    .newDraweeControllerBuilder()
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


        } catch (RuntimeException poster) {
            poster.printStackTrace();
        }

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
        LogUtils.newLog(TAG, "currentLocationTasks: ");
        registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));
        UserLocationManagerFused.getLocationInstance(MovieActivity.this).startLocationUpdates();
        mLocationAcquired = false;
        boolean enabled = UserLocationManagerFused.getLocationInstance(MovieActivity.this).isLocationEnabled();
        if (!enabled) {
            ProgressBar.setVisibility(View.GONE);
            selectedTheatersRecyclerView.setVisibility(View.GONE);
            enableLocation.setVisibility(View.VISIBLE);
            locationMsg.setVisibility(View.VISIBLE);
            arrow.setVisibility(View.VISIBLE);
            enableLocation.setOnClickListener(v -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)));

        } else {
            selectedTheatersRecyclerView.setVisibility(View.VISIBLE);
            enableLocation.setVisibility(View.GONE);
            locationMsg.setVisibility(View.GONE);
            arrow.setVisibility(View.GONE);

            Location location = UserLocationManagerFused.getLocationInstance(MovieActivity.this).mCurrentLocation;
            LogUtils.newLog(TAG, "currentLocationTasks: " + location);
            onLocationChanged(location);
            if (location != null) {
                LogUtils.newLog(TAG, "location not null: ");
                UserLocationManagerFused.getLocationInstance(this).requestLocationForCoords(location.getLatitude(), location.getLongitude(), MovieActivity.this);
            }
        }
    }


    private void showActivateCardDialog(Screening screening, String showtime) {
        Intent activateCard = new Intent(MovieActivity.this, ActivateMoviePassCard.class);
        activateCard.putExtra(SCREENING, Parcels.wrap(screening));
        activateCard.putExtra(SHOWTIME, showtime);
        startActivity(activateCard);


    }


}
