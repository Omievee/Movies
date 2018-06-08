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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
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
import com.mobile.adapters.MissingCheckinListener;
import com.mobile.adapters.TheaterScreeningsAdapter;
import com.mobile.fragments.SynopsisFragment;
import com.mobile.helpers.BottomNavigationViewHelper;
import com.mobile.helpers.GoWatchItSingleton;
import com.mobile.helpers.LogUtils;
import com.mobile.listeners.ShowtimeClickListener;
import com.mobile.model.Availability;
import com.mobile.model.Movie;
import com.mobile.model.Reservation;
import com.mobile.model.Screening;
import com.mobile.model.ScreeningToken;
import com.mobile.model.Theater;
import com.mobile.model.TicketType;
import com.mobile.network.RestCallback;
import com.mobile.network.RestClient;
import com.mobile.network.RestError;
import com.mobile.requests.TicketInfoRequest;
import com.mobile.responses.ReservationResponse;
import com.mobile.responses.ScreeningsResponseV2;
import com.mobile.seats.BringAFriendActivity;
import com.moviepass.R;

import org.jetbrains.annotations.NotNull;
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


public class MovieActivity extends BaseActivity implements ShowtimeClickListener, MissingCheckinListener {

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

    TheaterScreeningsAdapter movieTheatersAdapter = new TheaterScreeningsAdapter(this,this);

    ScreeningsResponseV2 screeningsResponse;
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
    View progress;

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
        progress = findViewById(R.id.progress);

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

    @Nullable private Pair<Screening, String> selected;

    public void setShowings(){
        progress.setVisibility(View.VISIBLE);
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

        if(screeningsResponse!=null) {
            movieTheatersAdapter.setData(TheaterScreeningsAdapter.Companion.createData(movieTheatersAdapter.getData(), screeningsResponse.getScreenings(), selected));
        }


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


    public void onShowtimeClick(@org.jetbrains.annotations.Nullable Theater theater, @NotNull final Screening screening, @NotNull final String showtime) {
        if (selected != null && screening.equals(selected.first) && showtime.equals(selected.second)) {
            selected = null;
        } else {
            selected = new Pair(screening, showtime);
        }
        LogUtils.newLog(TAG, "onShowtimeClick: " + UserPreferences.getRestrictionHasActiveCard());
        GoWatchItSingleton.getInstance().userClickedOnShowtime(theater, screening, showtime, String.valueOf(movie.getId()), url);
        if (buttonCheckIn.getVisibility() == View.GONE) {
            fadeIn(buttonCheckIn);
            buttonCheckIn.setVisibility(View.VISIBLE);
        }
        Availability availability = screening.getAvailability(showtime);
        if (availability.isETicket()) {
            buttonCheckIn.setText("Continue to E-Ticketing");
        } else {
            buttonCheckIn.setText("Check In");
        }
        buttonCheckIn.setEnabled(true);
        buttonCheckIn.setOnClickListener(view -> {

            if (isPendingSubscription() && availability.getTicketType()== TicketType.E_TICKET) {
                progress.setVisibility(View.VISIBLE);
                reserve(screening, showtime);
            } else if (isPendingSubscription() && availability.getTicketType()==TicketType.STANDARD) {
                showActivateCardDialog(screening, showtime);
            } else if (isPendingSubscription() && availability.getTicketType() == TicketType.SELECT_SEATING) {
                progress.setVisibility(View.VISIBLE);
                reserve(screening, showtime);
            } else if (availability.getTicketType()==TicketType.STANDARD) {
                if (UserPreferences.getProofOfPurchaseRequired() || screening.getPopRequired()) {
                    alertTicketVerifNotice(theater, screening, showtime);
                } else {
                    progress.setVisibility(View.VISIBLE);
                    reserve(screening, showtime);
                }
            } else {
                progress.setVisibility(View.VISIBLE);
                reserve(screening, showtime);
            }
        });
    }


    void alertTicketVerifNotice(Theater theater, Screening screening, String showtime) {
        AlertDialog.Builder alert = new AlertDialog.Builder(MovieActivity.this, R.style.CUSTOM_ALERT);
        alert.setView(R.layout.alertdialog_ticketverif);

        alert.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            progress.setVisibility(View.VISIBLE);
            reserve(screening, showtime);
        });

        alert.show();
    }


    public void reserve(Screening screening, String showtime) {
        Theater theaterObject = screeningsResponse.getTheater(screening);
        Screening screen = screening;
        String time = showtime;
        Context context = this;
        Location mCurrentLocation = UserLocationManagerFused.getLocationInstance(context).mCurrentLocation;

        if (mCurrentLocation != null) {
            UserLocationManagerFused.getLocationInstance(context).updateLocation(mCurrentLocation);
        } else {
            Toast.makeText(context, "NULL", Toast.LENGTH_SHORT).show();
        }

        buttonCheckIn.setEnabled(false);
        /* Standard Check In */

        Availability availability = screening.getAvailability(showtime);
        if (availability == null) {
            return;
        }
        if (availability.getTicketType() == com.mobile.model.TicketType.STANDARD) {
            if (isPendingSubscription()) {
                showActivateCardDialog(screening, showtime);
            }
            TicketInfoRequest checkInRequest = new TicketInfoRequest(availability.getProviderInfo(), null, null, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            reservationRequest(screen, checkInRequest, time);

        } else if (availability.getTicketType() == com.mobile.model.TicketType.E_TICKET) {
            progress.setVisibility(View.GONE);
            showEticketConfirmation(screen, time);

        } else {
            progress.setVisibility(View.GONE);
            Intent intent = BringAFriendActivity.Companion.newIntent(context, theaterObject, screening, time);
            startActivity(intent);
        }
    }


    private void showEticketConfirmation(Screening screeningObject, String selectedShowTime) {

        Intent intent = new Intent(this, EticketConfirmation.class);

        intent.putExtra(SCREENING, Parcels.wrap(screeningObject));
        intent.putExtra(SHOWTIME, selectedShowTime);

        startActivity(intent);
    }


    private void reservationRequest(final Screening screening, TicketInfoRequest checkInRequest, final String showtime) {
        Theater theaterObject = screeningsResponse.getTheater(screening);
        Context context = this;
        RestClient.getAuthenticated().checkIn(checkInRequest).enqueue(new RestCallback<ReservationResponse>() {
            @Override
            public void onResponse(Call<ReservationResponse> call, Response<ReservationResponse> response) {
                ReservationResponse reservationResponse = response.body();

                if (reservationResponse != null & response.isSuccessful()) {
                    reservation = reservationResponse.getReservation();
                    UserPreferences.saveReservation(new ScreeningToken(screening, reservationResponse.getShowtime(), reservation, theaterObject));
                    progress.setVisibility(View.GONE);

                    if (reservationResponse.getETicketConfirmation() != null) {

                        ScreeningToken token = new ScreeningToken(screening, showtime, reservation, reservationResponse.getETicketConfirmation(), theaterObject);
                        showConfirmation(token);
                        GoWatchItSingleton.getInstance().checkInEvent(theaterObject, screening, showtime, "ticket_purchase", String.valueOf(theaterObject.getId()), url);

                    } else {
                        ScreeningToken token = new ScreeningToken(screening, showtime, reservation, theaterObject);
                        showConfirmation(token);
                        GoWatchItSingleton.getInstance().checkInEvent(theaterObject, screening, showtime, "ticket_purchase", String.valueOf(theaterObject.getId()), url);
                    }
                } else {

                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        //PENDING RESERVATION GO TO TicketConfirmationActivity or TicketVerificationActivity
                        progress.setVisibility(View.GONE);
                        buttonCheckIn.setVisibility(View.VISIBLE);
                        buttonCheckIn.setEnabled(true);

                        //IF USER HASNT ACTIVATED CARD AND THEY TRY TO CHECK IN!
                        if (jObjError.getString("message").equals("You do not have an active card")) {
                            Toast.makeText(context, "You do not have an active card", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                            GoWatchItSingleton.getInstance().checkInEvent(theaterObject, screening, showtime, "ticket_purchase_attempt", String.valueOf(theaterObject.getId()), url);
                        }
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    progress.setVisibility(View.GONE);
                    buttonCheckIn.setVisibility(View.VISIBLE);
                    buttonCheckIn.setEnabled(true);
                }
                buttonCheckIn.setVisibility(View.VISIBLE);
                buttonCheckIn.setEnabled(true);
            }

            @Override
            public void failure(RestError restError) {
                progress.setVisibility(View.GONE);
                buttonCheckIn.setVisibility(View.VISIBLE);
                buttonCheckIn.setEnabled(true);

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
                    LogUtils.newLog("resResponse:", "else onfail:" + "onRespnse fail");
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


    private void loadTheaters(Double latitude, Double longitude, int moviepassId) {
        RestClient.getAuthenticated().getScreeningsForMovie(latitude, longitude, moviepassId).enqueue(new retrofit2.Callback<ScreeningsResponseV2>() {

            @Override
            public void onResponse(Call<ScreeningsResponseV2> call, final Response<ScreeningsResponseV2> response) {
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
                            TicketType type = etix.getTicketType();
                            if (type==TicketType.E_TICKET||type==TicketType.SELECT_SEATING) {
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
                        currentShowTimes = currentScreening.getAvailabilities().size();
                        if (currentScreening.getAvailabilities() != null) {

                            for (int j = 0; j < currentScreening.getAvailabilities().size(); j++) {
                                Availability availability = currentScreening.getAvailabilities().get(j);
                                try {
                                    Date systemClock = new Date();

                                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
                                    String curTime = sdf.format(systemClock);

                                    Date theaterTime = sdf.parse(availability.getStartTime());
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
                                if (!notApproved.getApproved()) {
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
                progress.setVisibility(View.GONE);


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
            public void onFailure(Call<ScreeningsResponseV2> call, Throwable t) {
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

    @Override
    public void onClick(@NotNull Screening screening, @NotNull String showTime) {
        onShowtimeClick(screeningsResponse.getTheater(screening), screening, showTime);
        movieTheatersAdapter.setData(TheaterScreeningsAdapter.Companion.createData(movieTheatersAdapter.getData(), screeningsResponse.getScreenings(), selected));
    }


    class LocationUpdateBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (mLocationBroadCast != null) {
                    unregisterReceiver(mLocationBroadCast);
                    mLocationBroadCast = null;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLocationBroadCast!=null) {
            try {
                unregisterReceiver(mLocationBroadCast);
            } catch (Exception ignore) {}
        }
    }

    public void currentLocationTasks() {
        LogUtils.newLog(TAG, "currentLocationTasks: ");
        registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));
        UserLocationManagerFused.getLocationInstance(MovieActivity.this).startLocationUpdates();
        mLocationAcquired = false;
        boolean enabled = UserLocationManagerFused.getLocationInstance(MovieActivity.this).isLocationEnabled();
        if (!enabled) {
            progress.setVisibility(View.GONE);
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
        activateCard.putExtra(SCREENING, screening);
        activateCard.putExtra(SHOWTIME, showtime);
        startActivity(activateCard);
    }


}
