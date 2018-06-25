package com.mobile.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.mobile.ApiError;
import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.activities.ActivateMoviePassCard;
import com.mobile.activities.ConfirmationActivity;
import com.mobile.adapters.MissingCheckinListener;
import com.mobile.adapters.TheaterScreeningsAdapter;
import com.mobile.helpers.GoWatchItSingleton;
import com.mobile.history.HistoryManager;
import com.mobile.history.model.ReservationHistory;
import com.mobile.listeners.ShowtimeClickListener;
import com.mobile.location.LocationManager;
import com.mobile.location.UserLocation;
import com.mobile.model.Availability;
import com.mobile.model.Movie;
import com.mobile.model.Reservation;
import com.mobile.model.Screening;
import com.mobile.model.ScreeningToken;
import com.mobile.model.Theater;
import com.mobile.model.TicketType;
import com.mobile.network.Api;
import com.mobile.requests.TicketInfoRequest;
import com.mobile.reservation.ReservationActivity;
import com.mobile.responses.ReservationResponse;
import com.mobile.responses.ScreeningsResponseV2;
import com.mobile.rx.Schedulers;
import com.mobile.seats.BringAFriendActivity;
import com.moviepass.R;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class MovieFragment extends MPFragment implements ShowtimeClickListener, MissingCheckinListener {

    //Constants
    public static final String MOVIE = "movie";
    public static final String TITLE = "title";
    public static final String RESERVATION = "reservation";
    public static final String DEEPLINK = "deep_link";
    public static final String THEATER = "theater";
    public static final String SCREENING = "screening";
    public static final String SHOWTIME = "showtime";
    public static final String CAMPAIGN = "campaign";
    public static final String TOKEN = "token";
    private static final String TAG = "MovieFragment";

    @Inject
    LocationManager locationManager;

    @Inject
    HistoryManager historyManager;

    @Inject
    Api api;

    private Location myLocation = new Location("");

    //Go Watch It
    String campaign = "no_campaign";
    Context myContext;
    //Current Movie
    public Movie movie;
    public String position;
    Screening screening;

    boolean isMovieComingSoon = false;


    // -- NOT USED --//
    //TODO REMOVE OR USE
    Reservation reservation;
    TheaterScreeningsAdapter movieTheatersAdapter;
    Pair<List<ReservationHistory>,ScreeningsResponseV2> screeningsResponse;

    TextView THEATER_ADDRESS_LISTITEM, noTheaters, enableLocation, locationMsg, noWifi;
    ImageView arrow;
    TextView selectedMovieTitle;
    View comingSoonScrollView, showtimeScrolls;
    TextView comingSoonTitle, synopsisTitle, synopsisContent;

    @BindView(R.id.SELECTED_THEATERS)
    RecyclerView selectedTheatersRecyclerView;

    @BindView(R.id.SELECTED_MOVIE_IMAGE)
    SimpleDraweeView selectedMoviePoster;

    @BindView(R.id.hours)
    TextView selectedRuntimeHours;

    @BindView(R.id.minutes)
    TextView selectedRuntimeMinutes;

    @BindView(R.id.button_check_in)
    Button buttonCheckIn;

    @BindView(R.id.SELECTED_SYNOPSIS)
    ImageButton selectedSynopsis;

    @BindView(R.id.progress)
    View progress;

    TextView filmRating;
    boolean synopsisShowing;

    private static final String MOVIE_PARAM = "param1";

    @Nullable
    Pair<Screening, String> selected;

    @Nullable
    Disposable locationSub;

    @Nullable
    Disposable reserveSub;

    @javax.annotation.Nullable
    Disposable theaterSub;

    public MovieFragment() {
        // Required empty public constructor
    }

    public static MovieFragment newInstance(Movie movie, String url) {
        MovieFragment fragment = new MovieFragment();
        Bundle args = new Bundle();
        args.putParcelable(MOVIE_PARAM, movie);
        fragment.setArguments(args);
        return fragment;
    }

    public static MovieFragment newInstance(Movie movie) {
        MovieFragment fragment = new MovieFragment();
        Bundle args = new Bundle();
        args.putParcelable(MOVIE_PARAM, movie);
        fragment.setArguments(args);
        return fragment;
    }

    public static MovieFragment newInstance(Screening screening) {
        MovieFragment fragment = new MovieFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.SCREENING, Parcels.wrap(screening));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {


            movie = getArguments().getParcelable(MOVIE_PARAM);
            screening = Parcels.unwrap(getArguments().getParcelable(Constants.SCREENING));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movie, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        setUpView(view);
        loadMoviePosterData();
        Date today = Calendar.getInstance().getTime();
        if (movie != null) {
            if (movie.getReleaseDate() != null) {
                SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
                try {
                    Date date = format1.parse(movie.getReleaseDate());
                    SimpleDateFormat format2 = new SimpleDateFormat("MMM dd, yyyy");
                    String result = format2.format(date);
                    if (date.before(today)) {
                        progress.setVisibility(View.VISIBLE);
                        setUpShowTimes();
                    } else {
                        isMovieComingSoon = true;
                        setUpComingSoon();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                progress.setVisibility(View.VISIBLE);
                setUpShowTimes();
            }
        } else {
            progress.setVisibility(View.VISIBLE);
            setUpShowTimes();
        }


        if (movie != null) {
            filmRating.setText(movie.getRating());
            selectedMovieTitle.setText(movie.getTitle());
            int t = movie.getRunningTime();
            int hours = t / 60;
            int minutes = t % 60;

            if (t == 0) {
                selectedRuntimeHours.setVisibility(View.GONE);
                selectedRuntimeMinutes.setVisibility(View.GONE);
            } else {
                selectedRuntimeHours.setText(Integer.toString(hours));
                selectedRuntimeMinutes.setText(Integer.toString(minutes));
            }
        } else {
            filmRating.setText(screening.getRating());
            selectedMovieTitle.setText(screening.getTitle());
            int t = screening.getRunningTime();
            int hours = t / 60;
            int minutes = t % 60;

            if (t == 0) {
                selectedRuntimeHours.setVisibility(View.GONE);
                selectedRuntimeMinutes.setVisibility(View.GONE);
            } else {
                selectedRuntimeHours.setText(Integer.toString(hours));
                selectedRuntimeMinutes.setText(Integer.toString(minutes));
            }
        }
        movieTheatersAdapter = new TheaterScreeningsAdapter(this, this);
        selectedTheatersRecyclerView.setAdapter(movieTheatersAdapter);
    }

    public void setUpComingSoon() {
        showtimeScrolls.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        comingSoonScrollView.setVisibility(View.VISIBLE);
        selectedSynopsis.setVisibility(View.GONE);
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
        try {
            if (movie != null) {
                Date date = format1.parse(movie.getReleaseDate());
                SimpleDateFormat format2 = new SimpleDateFormat("MMM dd, yyyy");
                String result = format2.format(date);
                comingSoonTitle.setText("In Theaters " + result);
                filmRating.setText(movie.getRating());
                synopsisContent.setText(movie.getSynopsis());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    void networkCheckBeforeLoad() {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) myContext.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo nInfo = Objects.requireNonNull(connectivityManager).getActiveNetworkInfo();

        if (nInfo != null) {
            isLocationEnabled();
        } else {
            progress.setVisibility(View.GONE);
            selectedTheatersRecyclerView.setVisibility(View.GONE);
            noWifi.setVisibility(View.VISIBLE);
        }
    }

    public void setUpShowTimes() {

        showtimeScrolls.setVisibility(View.VISIBLE);

        /* Theaters RecyclerView */
        LinearLayoutManager moviesLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        selectedTheatersRecyclerView.setLayoutManager(moviesLayoutManager);
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);
        selectedTheatersRecyclerView.setItemAnimator(itemAnimator);
        selectedTheatersRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        UserLocation loc = locationManager.lastLocation();
        if (loc != null) {
            myLocation.setLatitude(loc.getLat());
            myLocation.setLongitude(loc.getLon());
        }
        if (movie != null) {
            if (movie.getReleaseDate() == null || !isMovieComingSoon) {
                if (myLocation != null) {
                    networkCheckBeforeLoad();
                }
            }
        } else {
            networkCheckBeforeLoad();
        }

    }

    public void isLocationEnabled() {
        if (!locationManager.isLocationEnabled()) {
            noWifi.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            selectedTheatersRecyclerView.setVisibility(View.GONE);
            locationMsg.setVisibility(View.VISIBLE);
            enableLocation.setVisibility(View.VISIBLE);
            enableLocation.setOnClickListener(v -> {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            });
        } else {
            locationMsg.setVisibility(View.GONE);
            enableLocation.setVisibility(View.GONE);
            noWifi.setVisibility(View.GONE);
            selectedTheatersRecyclerView.setVisibility(View.VISIBLE);
            UserLocation location = locationManager.lastLocation();
            double lat = 0;
            double lon = 0;
            if (location != null) {
                lat = location.getLat();
                lon = location.getLon();
                if (movie != null) {
                    loadTheaters(lat, lon, movie.getId());
                } else {
                    loadTheaters(lat, lon, screening.getMoviepassId());
                }
            } else {
                fetchLocation();
            }


        }
    }

    private void fetchLocation() {
        if (locationSub != null) {
            locationSub.dispose();
        }
        locationSub = locationManager
                .location()
                .subscribe(l -> {
                    myLocation.setLatitude(l.getLat());
                    myLocation.setLongitude(l.getLon());
                    loadTheaters(l.getLat(), l.getLon(), movie != null ? movie.getId() : screening.getMoviepassId());
                }, e -> {

                });
    }

    public void setUpView(View view) {
        arrow = view.findViewById(R.id.arrow);
        enableLocation = view.findViewById(R.id.EnableText);
        locationMsg = view.findViewById(R.id.GPSmessage);
        noTheaters = view.findViewById(R.id.NoTheaters);
        selectedMoviePoster = view.findViewById(R.id.SELECTED_MOVIE_IMAGE);
        selectedMovieTitle = view.findViewById(R.id.SELECTED_MOVIE_TITLE);
        THEATER_ADDRESS_LISTITEM = view.findViewById(R.id.THEATER_ADDRESS2_LISTITEM);
        selectedRuntimeMinutes = view.findViewById(R.id.minutes);
        selectedRuntimeHours = view.findViewById(R.id.hours);
        buttonCheckIn = view.findViewById(R.id.button_check_in);
        progress = view.findViewById(R.id.progress);
        selectedTheatersRecyclerView = view.findViewById(R.id.SELECTED_THEATERS);
        filmRating = view.findViewById(R.id.SELECTED_FILM_RATING);
        selectedSynopsis = view.findViewById(R.id.SELECTED_SYNOPSIS);
        synopsisShowing = false;
        selectedSynopsis.setClickable(false);
        comingSoonScrollView = view.findViewById(R.id.comingSoon);
        comingSoonTitle = view.findViewById(R.id.comingSoonTitle);
        synopsisTitle = view.findViewById(R.id.synopsisTitle);
        synopsisContent = view.findViewById(R.id.synopsisContent);
        showtimeScrolls = view.findViewById(R.id.NESTED_SCROLL);
        noWifi = view.findViewById(R.id.no_wifi);
    }

    public void onShowtimeClick(Theater theater, final Screening screening, final String showtime) {
        if (selected != null && screening.equals(selected.first) && showtime.equals(selected.second)) {
            selected = null;
        } else {
            selected = new Pair<>(screening, showtime);
        }
        movieTheatersAdapter.setData(TheaterScreeningsAdapter.Companion.createData(movieTheatersAdapter.getData(), screeningsResponse, myLocation, selected));
        if (movie != null) {
            GoWatchItSingleton.getInstance().userClickedOnShowtime(theater, screening, showtime, String.valueOf(movie.getId()), "");
        } else {
            GoWatchItSingleton.getInstance().userClickedOnShowtime(theater, screening, showtime, String.valueOf(screening.getMoviepassId()), "");
        }

        if (selected != null) {
            fadeIn(buttonCheckIn);
        } else {
            fadeOut(buttonCheckIn);
        }
        Availability availability = screening.getAvailability(showtime);
        if (availability.isETicket() || availability.getTicketType() == TicketType.SELECT_SEATING) {
            buttonCheckIn.setText("Continue to E-Ticketing");
        } else {
            buttonCheckIn.setText("Check In");
        }
        buttonCheckIn.setEnabled(true);
        buttonCheckIn.setOnClickListener(view -> {

            if (isPendingSubscription() && availability.getTicketType() == TicketType.E_TICKET) {
                progress.setVisibility(View.VISIBLE);
                reserve(screening, showtime);
            } else if (isPendingSubscription() && availability.getTicketType() == TicketType.STANDARD) {
                showActivateCardDialog(screening, showtime);
            } else if (isPendingSubscription() && availability.getTicketType() == TicketType.SELECT_SEATING) {
                progress.setVisibility(View.VISIBLE);
                reserve(screening, showtime);
            } else if (availability.getTicketType() == TicketType.STANDARD) {
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

    public boolean isPendingSubscription() {
        if (UserPreferences.getRestrictionSubscriptionStatus().matches("PENDING_ACTIVATION") ||
                UserPreferences.getRestrictionSubscriptionStatus().matches("PENDING_FREE_TRIAL")) {
            return true;
        } else {
            return false;
        }
    }

    void alertTicketVerifNotice(Theater theater, Screening screening, String showtime) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), R.style.CUSTOM_ALERT);
        alert.setView(R.layout.alertdialog_ticketverif);

        alert.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            progress.setVisibility(View.VISIBLE);
            reserve(screening, showtime);
        });

        alert.show();
    }

    public void reserve(Screening screening, String showtime) {
        Theater theaterObject = screeningsResponse.second.getTheater(screening);
        Screening screen = screening;
        String time = showtime;
        Context context = getActivity();


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
            if (myLocation != null) {
                TicketInfoRequest checkInRequest = new TicketInfoRequest(availability.getProviderInfo(), null, null, myLocation.getLatitude(), myLocation.getLongitude());
                reservationRequest(screen, checkInRequest, time);
            } else {
                Toast.makeText(context, "Enable GPS to check in.", Toast.LENGTH_SHORT).show();
            }


        } else if (availability.getTicketType() == com.mobile.model.TicketType.E_TICKET) {
            progress.setVisibility(View.GONE);
            showConfirmation(new ScreeningToken(screening, showtime, reservation, theaterObject));
        } else {
            progress.setVisibility(View.GONE);
            Intent intent = BringAFriendActivity.Companion.newIntent(context, theaterObject, screening, time);
            startActivity(intent);
        }
    }

    private void reservationRequest(final Screening screening, TicketInfoRequest checkInRequest, final String showtime) {
        Theater theaterObject = screeningsResponse.second.getTheater(screening);
        Context context = getActivity();
        if (reserveSub != null) {
            reserveSub.dispose();
        }
        reserveSub = api.reserve(checkInRequest)
                .doAfterTerminate(()-> {
                    buttonCheckIn.setEnabled(true);
                    progress.setVisibility(View.GONE);
                })
                .subscribe(result -> {
                    ReservationResponse reservationResponse = result;

                    reservation = reservationResponse.getReservation();
                    UserPreferences.saveReservation(new ScreeningToken(screening, reservationResponse.getShowtime(), reservation, theaterObject));


                    if (reservationResponse.getETicketConfirmation() != null) {

                        ScreeningToken token = new ScreeningToken(screening, showtime, reservation, reservationResponse.getETicketConfirmation(), theaterObject);
                        showConfirmation(token);
                        GoWatchItSingleton.getInstance().checkInEvent(theaterObject, screening, showtime, "ticket_purchase", String.valueOf(theaterObject.getId()), "");

                    } else {
                        ScreeningToken token = new ScreeningToken(screening, showtime, reservation, theaterObject);
                        showConfirmation(token);
                        GoWatchItSingleton.getInstance().checkInEvent(theaterObject, screening, showtime, "ticket_purchase", String.valueOf(theaterObject.getId()), "");
                    }

                    progress.setVisibility(View.GONE);
                    buttonCheckIn.setVisibility(View.VISIBLE);
                    buttonCheckIn.setEnabled(true);
                }, error -> {
                    progress.setVisibility(View.GONE);
                    buttonCheckIn.setVisibility(View.VISIBLE);
                    buttonCheckIn.setEnabled(true);
                    if (error instanceof ApiError) {
                        ApiError apiError = (ApiError) error;
                        Toast.makeText(myContext, apiError.getError().getMessage(), Toast.LENGTH_SHORT).show();
                        if (apiError.getMessage() != null && apiError.getMessage().contains("active card")) {
                        } else {
                            GoWatchItSingleton.getInstance().checkInEvent(theaterObject, screening, showtime, "ticket_purchase_attempt", String.valueOf(theaterObject.getId()), "");
                        }
                    }
                    progress.setVisibility(View.GONE);
                    buttonCheckIn.setVisibility(View.VISIBLE);
                });
    }


    private void showConfirmation(ScreeningToken token) {
        if (token.getConfirmationCode() != null && !TextUtils.isEmpty(token.getConfirmationCode().getConfirmationCode())) {
            startActivity(ReservationActivity.Companion.newInstance(myContext, token));
        } else {
            startActivity(new Intent(myContext, ConfirmationActivity.class).putExtra(Constants.TOKEN, Parcels.wrap(token)));
        }
        Activity activity = getActivity();
        if (activity != null) {
            activity.onBackPressed();
        }
    }

    private void loadTheaters(Double latitude, Double longitude, int moviepassId) {
        theaterSub = Observable.zip(
                historyManager.getHistory(),
                api.getScreeningsForMovieRx(latitude, longitude, moviepassId).toObservable(),
                (history, screeningsResponse) -> new Pair<>(history, screeningsResponse))
                .compose(Schedulers.Companion.observableDefault())
                .doAfterTerminate(() -> progress.setVisibility(View.GONE))
                .subscribe(res -> {
                    screeningsResponse = res;
                    screeningsResponse.second.mapMoviepassId(moviepassId);
                    onScreeningsResponse();
                }, error -> {
                    error.printStackTrace();
                });
    }

    private void onScreeningsResponse() {
        ScreeningsResponseV2 response = screeningsResponse.second;
        if (response.getScreenings().size() == 0) {
            selectedTheatersRecyclerView.setVisibility(View.GONE);
            noTheaters.setVisibility(View.VISIBLE);
        } else {
            noTheaters.setVisibility(View.GONE);
            movieTheatersAdapter.setData(TheaterScreeningsAdapter.Companion.createData(movieTheatersAdapter.getData(), screeningsResponse, myLocation, selected));
        }

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
                FragmentManager fm = getChildFragmentManager();
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
                FragmentManager fm = getChildFragmentManager();
                fragobj.show(fm, "fr_dialogfragment_synopsis");

            });
        }
    }

    private void loadMoviePosterData() {
        final Uri imgUrl;
        if (movie != null) {
            imgUrl = Uri.parse(movie.getLandscapeImageUrl());

        } else {
            imgUrl = Uri.parse(screening.getLandscapeImageUrl());
        }
        selectedMoviePoster.setImageURI(imgUrl);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, @org.jetbrains.annotations.Nullable ImageInfo imageInfo, @org.jetbrains.annotations.Nullable Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);

                        if (imgUrl.toString().contains("updateMovieThumb")) {
                            //TODO DONT KNOW ABOUT THIS
//                            supportStartPostponedEnterTransition();
                            selectedMoviePoster.setImageResource(R.drawable.film_reel_icon);
                            selectedMoviePoster.animate();
                            if (movie != null) {
                                selectedMovieTitle.setText(movie.getTitle());
                            } else {
                                selectedMovieTitle.setText(screening.getTitle());
                            }

                        } else {
                            //TODO DONT KNOW ABOUT THIS
//                            supportStartPostponedEnterTransition();
                            selectedMoviePoster.animate();
                            selectedMoviePoster.setImageURI(imgUrl);
                            if (movie != null) {
                                selectedMovieTitle.setText(movie.getTitle());
                            } else {
                                selectedMovieTitle.setText(screening.getTitle());
                            }
                            selectedMoviePoster.getHierarchy().setFadeDuration(200);
                        }
                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        //TODO DONT KNOW ABOUT THIS
//                        supportStartPostponedEnterTransition();
                        selectedMoviePoster.setImageResource(R.drawable.film_reel_icon);
                        if (movie != null) {
                            selectedMovieTitle.setText(movie.getTitle());
                        } else {
                            selectedMovieTitle.setText(screening.getTitle());
                        }
                    }
                })
                .setUri(imgUrl)
                .build();
        selectedMoviePoster.setController(controller);
    }

    private void showActivateCardDialog(Screening screening, String showtime) {
        Intent activateCard = new Intent(getActivity(), ActivateMoviePassCard.class);
        activateCard.putExtra(SCREENING, Parcels.wrap(screening));
        activateCard.putExtra(SHOWTIME, showtime);
        startActivity(activateCard);
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
        myContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        myContext = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (reserveSub != null) {
            reserveSub.dispose();
        }
        if (locationSub != null) {
            locationSub.dispose();
        }
        if (theaterSub!=null) {
            theaterSub.dispose();
        }
    }

    @Override
    public void onClick(@NotNull Screening screening, @NotNull String showTime) {
    }

}