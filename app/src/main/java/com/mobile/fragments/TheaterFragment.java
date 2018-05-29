package com.mobile.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.Constants;
import com.mobile.UserLocationManagerFused;
import com.mobile.UserPreferences;
import com.mobile.activities.ConfirmationActivity;
import com.mobile.activities.EticketConfirmation;
import com.mobile.activities.SelectSeatActivity;
import com.mobile.activities.TicketType;
import com.mobile.adapters.MissingCheckinListener;
import com.mobile.adapters.TheaterScreeningsAdapter;
import com.mobile.helpers.ContextSingleton;
import com.mobile.helpers.GoWatchItSingleton;
import com.mobile.helpers.LogUtils;
import com.mobile.listeners.ShowtimeClickListener;
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
import com.mobile.reservation.ReservationActivity;
import com.mobile.responses.CardActivationResponse;
import com.mobile.responses.ReservationResponse;
import com.mobile.responses.ScreeningsResponse;
import com.moviepass.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import javax.annotation.Nullable;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by anubis on 6/8/17.
 */

public class TheaterFragment extends Fragment implements ShowtimeClickListener, MissingCheckinListener {
    public static final String TAG = "found it";
    Theater theaterObject;
    ScreeningsResponse screeningsResponse;
    RecyclerView theaterSelectedRecyclerView;
    ImageView cinemaPin, eTicketingIcon, reserveSeatIcon;
    TextView theaterSelectedAddress, theaterSelectedAddressZip, noTheaters;
    LinearLayoutManager theaterSelectedMovieManager;
    TheaterScreeningsAdapter theaterMoviesAdapter;
    boolean qualifiersApproved;
    Button buttonCheckIn;
    Screening screening = new Screening();
    LinkedList<Screening> moviesAtSelectedTheater;
    ArrayList<String> showtimesAtSelectedTheater;
    View progress;
    Activity myActivity;
    Context myContext;
    Reservation reservation;
    PerformanceInfoRequest mPerformReq;
    String url;
    @Nullable
    Pair<Screening, String> selected;

    public static final String POLICY = "policy";
    public static final String TOKEN = "token";
    public static final String SCREENING = "screening";
    public static final String SHOWTIME = "showtime";
    public static final String THEATER = "cinema";

    public static TheaterFragment newInstance() {
        return new TheaterFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fr_theater, container, false);
        ButterKnife.bind(this, rootView);

        //Object & Lists
        theaterObject = Parcels.unwrap(myActivity.getIntent().getParcelableExtra(THEATER));
        moviesAtSelectedTheater = new LinkedList<>();
        showtimesAtSelectedTheater = new ArrayList<>();
        cinemaPin = rootView.findViewById(R.id.CINEMA_PIN);

        buttonCheckIn = rootView.findViewById(R.id.button_check_in);
        progress = rootView.findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);
        eTicketingIcon = rootView.findViewById(R.id.CINEMA_E_TICKETING);
        reserveSeatIcon = rootView.findViewById(R.id.CINEMA_RES_SEATS);

        if (theaterObject.ticketTypeIsStandard()) {
            eTicketingIcon.setVisibility(View.INVISIBLE);
            reserveSeatIcon.setVisibility(View.INVISIBLE);
        } else if (theaterObject.ticketTypeIsETicket()) {
            reserveSeatIcon.setVisibility(View.INVISIBLE);
        }
        //Textviews
        theaterSelectedAddress = rootView.findViewById(R.id.CINEMA_ADDRESS);
        theaterSelectedAddressZip = rootView.findViewById(R.id.CINEMA_ZIPCITY);
        theaterSelectedAddress.setText(theaterObject.getAddress());
        theaterSelectedAddressZip.setText(theaterObject.getCity() + " " + theaterObject.getState() + " " + theaterObject.getZip());
        noTheaters = rootView.findViewById(R.id.NoTheaters);
        final Uri uri = Uri.parse("geo:" + theaterObject.getLat() + "," + theaterObject.getLon() + "?q=" + Uri.encode(theaterObject.getName()));

        cinemaPin.setOnClickListener(v -> {
            try {
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(uri)));
                mapIntent.setPackage("com.google.android.apps.maps");
                myActivity.startActivity(mapIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(myActivity, "Google Maps isn't installed", Toast.LENGTH_SHORT).show();
            } catch (Exception x) {
                x.getMessage();
            }
        });

        /* Start Location Tasks */
        UserLocationManagerFused.getLocationInstance(getContext()).startLocationUpdates();

        //Recycler / Adapter / LLM
        int resId = R.anim.layout_anim_bottom;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), resId);
        theaterSelectedRecyclerView = rootView.findViewById(R.id.CINEMA_SELECTED_THEATER_RECYCLER);
        theaterSelectedMovieManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        theaterMoviesAdapter = new TheaterScreeningsAdapter(this, this);
        theaterSelectedRecyclerView.setLayoutManager(theaterSelectedMovieManager);
        theaterSelectedRecyclerView.setAdapter(theaterMoviesAdapter);
        theaterSelectedRecyclerView.setLayoutAnimation(animation);
        SimpleItemAnimator animator = new DefaultItemAnimator();
        animator.setSupportsChangeAnimations(false);
        theaterSelectedRecyclerView.setItemAnimator(animator);
        theaterSelectedRecyclerView.setNestedScrollingEnabled(false);

        loadMovies();

        if (theaterObject.getName().contains("Flix Brewhouse")) {
            String theater = theaterObject.getName();
            Bundle bundle = new Bundle();
            bundle.putString(POLICY, theater);

            TheaterPolicy fragobj = new TheaterPolicy();
            fragobj.setArguments(bundle);
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fragobj.show(fm, "fr_theaterpolicy");
        }


        ContextSingleton.getInstance(getContext()).getGlobalContext();

        url = "https://moviepass.com/go/theaters/" + theaterObject.getId();
        if (!GoWatchItSingleton.getInstance().getCampaign().equalsIgnoreCase("no_campaign"))
            url = url + "/" + GoWatchItSingleton.getInstance().getCampaign();


        GoWatchItSingleton.getInstance().userOpenedTheater(theaterObject, url);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myContext = context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myActivity = activity;
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onShowtimeClick(@org.jetbrains.annotations.Nullable Theater theater, int pos, @NotNull final Screening screening, @NotNull final String showtime) {
        if (selected != null && screening.equals(selected.first) && showtime.equals(selected.second)) {
            selected = null;
        } else {
            selected = new Pair(screening, showtime);
        }
        theaterMoviesAdapter.setData(TheaterScreeningsAdapter.Companion.createData(theaterMoviesAdapter.getData(), screeningsResponse.getScreenings(), selected));
        if (selected == null) {
            fadeOut(buttonCheckIn);
            return;
        }
        final String time = showtime;
        final Screening screening1 = screening;

        LogUtils.newLog(TAG, "onShowtimeClick: ");

        if (selected != null) {
            fadeIn(buttonCheckIn);
        }

        if (screening.getProvider().ticketTypeIsSelectSeating() || screening.getProvider().ticketTypeIsETicket()) {
            buttonCheckIn.setText("Continue to E-Ticketing");
        } else {
            buttonCheckIn.setText("Check In");
        }
        buttonCheckIn.setEnabled(true);
        GoWatchItSingleton.getInstance().userClickedOnShowtime(theaterObject, screening, showtime, String.valueOf(screening.getMoviepassId()), url);
        buttonCheckIn.setOnClickListener(view -> {
            LogUtils.newLog(TAG, "onClick: " + screening.getProvider().ticketType);
            if (isPendingSubscription() && screening.getProvider().ticketType.matches("E_TICKET")) {
                progress.setVisibility(View.VISIBLE);
                reserve(screening, showtime);
            } else if (isPendingSubscription() && screening.getProvider().ticketType.matches("STANDARD")) {
                showActivateCardDialog(screening, showtime);
            } else if (isPendingSubscription() && screening.getProvider().ticketType.matches("SELECT_SEATING")) {
                progress.setVisibility(View.VISIBLE);
                reserve(screening, showtime);
            } else if (screening.getProvider().ticketType.matches("STANDARD")) {
                if (UserPreferences.getProofOfPurchaseRequired() || screening.isPopRequired()) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(myContext, R.style.CUSTOM_ALERT);
                    alert.setView(R.layout.alertdialog_ticketverif);
                    alert.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        progress.setVisibility(View.VISIBLE);
                        reserve(screening, showtime);
                    });
                    alert.show();
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

    private void loadMovies() {
        int theaterId = theaterObject.getTribuneTheaterId();
        RestClient.getAuthenticated().getScreeningsForTheater(theaterId).enqueue(new Callback<ScreeningsResponse>() {
            @Override
            public void onResponse(Call<ScreeningsResponse> call, Response<ScreeningsResponse> response) {
                screeningsResponse = response.body();
                theaterMoviesAdapter.setData(TheaterScreeningsAdapter.Companion.createData(theaterMoviesAdapter.getData(), screeningsResponse.getScreenings(), selected));

                progress.setVisibility(View.GONE);
                noTheaters.setVisibility(View.GONE);

                theaterSelectedRecyclerView.setVisibility(View.VISIBLE);

            }

            @Override
            public void onFailure(Call<ScreeningsResponse> call, Throwable t) {
                t.printStackTrace();
                progress.setVisibility(View.GONE);
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        buttonCheckIn.setEnabled(true);
    }

    public void reserve(Screening screening, String showtime) {
        Screening screen = screening;
        String time = showtime;
        Location mCurrentLocation = UserLocationManagerFused.getLocationInstance(getContext()).mCurrentLocation;

        if (mCurrentLocation != null) {
            UserLocationManagerFused.getLocationInstance(getContext()).updateLocation(mCurrentLocation);
        } else {
            Toast.makeText(myContext, "NULL", Toast.LENGTH_SHORT).show();
        }

        buttonCheckIn.setEnabled(false);
        /* Standard Check In */
        String providerName = screening.getProvider().providerName;
        //PerformanceInfo
        checkProviderDoPerformanceInfoRequest(screening, showtime);

        if (screening.getProvider().ticketType.matches("STANDARD")) {
            if (isPendingSubscription()) {
                showActivateCardDialog(screening, showtime);
            }
            TicketInfoRequest ticketInfo = new TicketInfoRequest(mPerformReq);
            CheckInRequest checkInRequest = new CheckInRequest(ticketInfo, providerName, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            reservationRequest(screen, checkInRequest, time);

        } else if (screening.getProvider().ticketType.matches("E_TICKET")) {
            progress.setVisibility(View.GONE);
            showEticketConfirmation(screen, time);

        } else {
            progress.setVisibility(View.GONE);
            Intent intent = new Intent(myActivity, SelectSeatActivity.class);
            intent.putExtra(SCREENING, Parcels.wrap(screen));
            intent.putExtra(SHOWTIME, time);
            intent.putExtra(THEATER, Parcels.wrap(theaterObject));
            startActivity(intent);
        }
    }

    private void reservationRequest(final Screening screening, CheckInRequest checkInRequest, final String showtime) {
        RestClient.getAuthenticated().checkIn(checkInRequest).enqueue(new RestCallback<ReservationResponse>() {
            @Override
            public void onResponse(Call<ReservationResponse> call, Response<ReservationResponse> response) {
                ReservationResponse reservationResponse = response.body();

                if (reservationResponse != null & response.isSuccessful()) {
                    reservation = reservationResponse.getReservation();
                    UserPreferences.saveReservation(new ScreeningToken(screening, reservationResponse.getShowtime(), reservation, theaterObject));
                    progress.setVisibility(View.GONE);

                    if (reservationResponse.getE_ticket_confirmation() != null) {

                        ScreeningToken token = new ScreeningToken(screening, showtime, reservation, reservationResponse.getE_ticket_confirmation(), theaterObject);
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
                            Toast.makeText(myActivity, "You do not have an active card", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(myActivity, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                            GoWatchItSingleton.getInstance().checkInEvent(theaterObject, screening, showtime, "ticket_purchase_attempt", String.valueOf(theaterObject.getId()), url);
                        }
                    } catch (Exception e) {
                        Toast.makeText(myActivity, e.getMessage(), Toast.LENGTH_LONG).show();
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

    public boolean isPendingSubscription() {
        if (UserPreferences.getRestrictionSubscriptionStatus().matches("PENDING_ACTIVATION") ||
                UserPreferences.getRestrictionSubscriptionStatus().matches("PENDING_FREE_TRIAL")) {
            return true;
        } else {
            return false;
        }
    }

    private void showActivateCardDialog(final Screening screening, final String showtime) {
        View dialoglayout = getLayoutInflater().inflate(R.layout.dialog_activate_card, null);
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(getContext());
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
                                Toast.makeText(getContext(), R.string.dialog_activate_card_successful, Toast.LENGTH_LONG).show();
                                reserve(screening, showtime);
                            } else {
                                Toast.makeText(getContext(), R.string.dialog_activate_card_bad_four_digits, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<CardActivationResponse> call, Throwable t) {
                            progress.setVisibility(View.GONE);

                            showActivateCardDialog(screening, showtime);
                        }
                    });
                } else {
                    Toast.makeText(getContext(), R.string.dialog_activate_card_must_enter_four_digits, Toast.LENGTH_LONG).show();
                }
            }
        });
        alert.setNegativeButton("Activate Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                Toast.makeText(getContext(), R.string.dialog_activate_card_must_activate_standard_theater, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        alert.show();
    }


    protected PerformanceInfoRequest checkProviderDoPerformanceInfoRequest(Screening screen, String time) {


        if (screen.getProvider().getProviderName().equalsIgnoreCase("MOVIEXCHANGE")) {
            int normalizedMovieId = screen.getMoviepassId();
            String externalMovieId = screen.getProvider().getPerformanceInfo(time).getExternalMovieId();
            String format = screen.getFormat();
            int tribuneTheaterId = screen.getTribuneTheaterId();
            int screeningId = screen.getProvider().getPerformanceInfo(time).getScreeningId();
            int performanceNumber = screen.getProvider().getPerformanceInfo(time).getPerformanceNumber();
            String sku = screen.getProvider().getPerformanceInfo(time).getSku();
            Double price = screen.getProvider().getPerformanceInfo(time).getPrice();
            String dateTime = screen.getProvider().getPerformanceInfo(time).getDateTime();
            String auditorium = screen.getProvider().getPerformanceInfo(time).getAuditorium();
            String performanceId = screen.getProvider().getPerformanceInfo(time).getPerformanceId();
            String sessionId = screen.getProvider().getPerformanceInfo(time).getSessionId();
            int theater = screen.getProvider().getTheater();
            String cinemaChainId = screen.getProvider().getPerformanceInfo(time).getCinemaChainId();
            String showtimeId = screen.getProvider().getPerformanceInfo(time).getShowtimeId();
            TicketType ticketType = screen.getProvider().getPerformanceInfo(time).getTicketType();


            mPerformReq = new PerformanceInfoRequest(
                    normalizedMovieId,
                    externalMovieId,
                    format,
                    tribuneTheaterId,
                    screeningId,
                    dateTime,
                    performanceNumber,
                    sku,
                    price,
                    auditorium,
                    performanceId,
                    sessionId,
                    cinemaChainId,
                    ticketType,
                    showtimeId);

            LogUtils.newLog(TAG, "----------------------------------------------: ");
            LogUtils.newLog(TAG, "provider: " + screen.getProvider().getProviderName());
            LogUtils.newLog(TAG, "normal: " + normalizedMovieId);
            LogUtils.newLog(TAG, "external: " + externalMovieId);
            LogUtils.newLog(TAG, "format: " + format);
            LogUtils.newLog(TAG, "tribune: " + tribuneTheaterId);
            LogUtils.newLog(TAG, "sku ID: " + sku);
            LogUtils.newLog(TAG, "price: " + price);
            LogUtils.newLog(TAG, "date: " + dateTime);
            LogUtils.newLog(TAG, "aud: " + auditorium);
            LogUtils.newLog(TAG, "perform: " + performanceId);
            LogUtils.newLog(TAG, "session: " + sessionId);
            LogUtils.newLog(TAG, "cinema: " + cinemaChainId);
            LogUtils.newLog(TAG, "show id: " + showtimeId);
            LogUtils.newLog(TAG, "tick type: " + ticketType);

            return mPerformReq;


        } else {
            //IF not movieXchange then it will simply request these parameters:
            int normalizedMovieId = screen.getMoviepassId();
            String externalMovieId = screen.getProvider().getPerformanceInfo(time).getExternalMovieId();
            String format = screen.getFormat();
            int tribuneTheaterId = screen.getTribuneTheaterId();
            int performanceNumber = screen.getProvider().getPerformanceInfo(time).getPerformanceNumber();
            String sku = screen.getProvider().getPerformanceInfo(time).getSku();
            Double price = screen.getProvider().getPerformanceInfo(time).getPrice();
            String dateTime = screen.getProvider().getPerformanceInfo(time).getDateTime();
            String auditorium = screen.getProvider().getPerformanceInfo(time).getAuditorium();
            String performanceId = screen.getProvider().getPerformanceInfo(time).getPerformanceId();
            String sessionId = screen.getProvider().getPerformanceInfo(time).getSessionId();

            mPerformReq = new PerformanceInfoRequest(
                    dateTime,
                    externalMovieId,
                    performanceNumber,
                    tribuneTheaterId,
                    format,
                    normalizedMovieId,
                    sku,
                    price,
                    auditorium,
                    performanceId,
                    sessionId);

            LogUtils.newLog(TAG, "----------------------------------------------: ");
            LogUtils.newLog(TAG, "time?: " + time);

            LogUtils.newLog(TAG, "provider: " + screen.getProvider().getProviderName());
            LogUtils.newLog(TAG, "normal: " + normalizedMovieId);
            LogUtils.newLog(TAG, "external: " + externalMovieId);
            LogUtils.newLog(TAG, "format: " + format);
            LogUtils.newLog(TAG, "tribune: " + tribuneTheaterId);
            LogUtils.newLog(TAG, "sku ID: " + sku);
            LogUtils.newLog(TAG, "price: " + price);
            LogUtils.newLog(TAG, "date: " + dateTime);
            LogUtils.newLog(TAG, "aud: " + auditorium);
            LogUtils.newLog(TAG, "perform: " + performanceId);
            LogUtils.newLog(TAG, "session: " + sessionId);
            LogUtils.newLog(TAG, "----------------------------------------------: ");

            return mPerformReq;
        }

    }

    private void showConfirmation(ScreeningToken token) {
        if (token.getConfirmationCode() != null && !TextUtils.isEmpty(token.getConfirmationCode().getConfirmationCode())) {
            startActivity(ReservationActivity.Companion.newInstance(myActivity, token));
        } else {
            startActivity(new Intent(myActivity, ConfirmationActivity.class).putExtra(Constants.TOKEN, Parcels.wrap(token)));
        }
        myActivity.finish();
    }

    private void showEticketConfirmation(Screening screeningObject, String selectedShowTime) {

        Intent intent = new Intent(myActivity, EticketConfirmation.class);

        intent.putExtra(SCREENING, Parcels.wrap(screeningObject));
        intent.putExtra(SHOWTIME, selectedShowTime);

        startActivity(intent);

    }

    public void fadeIn(View view) {
        Animation old = view.getAnimation();
        if(old!=null) {
            old.setAnimationListener(null);
            old.cancel();
        }
        if (view.getVisibility() == View.VISIBLE) {
            return;
        }
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(500);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animation.addAnimation(fadeIn);
        view.setAnimation(animation);

    }

    public void fadeOut(View view) {
        Animation old = view.getAnimation();
        if (old != null) {
            old.setAnimationListener(null);
            old.cancel();
        }
        if (view.getVisibility() != View.VISIBLE) {
            return;
        }
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new DecelerateInterpolator()); //add this
        fadeOut.setDuration(500);
        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeOut);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.setAnimation(animation);
    }


    @Override
    public void onClick(@NotNull Screening screening, @NotNull String showTime) {
        onShowtimeClick(null, 0, screening, showTime);
        theaterMoviesAdapter.setData(TheaterScreeningsAdapter.Companion.createData(theaterMoviesAdapter.getData(), screeningsResponse.getScreenings(), selected));
    }
}