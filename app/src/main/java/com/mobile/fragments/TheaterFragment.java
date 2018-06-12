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
import android.view.animation.AnimationUtils;
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
import com.mobile.adapters.EticketTheatersAdapter;
import com.mobile.adapters.MissingCheckinListener;
import com.mobile.adapters.TheaterScreeningsAdapter;
import com.mobile.helpers.ContextSingleton;
import com.mobile.helpers.GoWatchItSingleton;
import com.mobile.helpers.LogUtils;
import com.mobile.listeners.ShowtimeClickListener;
import com.mobile.model.Availability;
import com.mobile.model.Reservation;
import com.mobile.model.Screening;
import com.mobile.model.ScreeningToken;
import com.mobile.model.Theater;
import com.mobile.network.RestCallback;
import com.mobile.network.RestClient;
import com.mobile.network.RestError;
import com.mobile.requests.CardActivationRequest;
import com.mobile.requests.TicketInfoRequest;
import com.mobile.reservation.ReservationActivity;
import com.mobile.responses.CardActivationResponse;
import com.mobile.responses.ReservationResponse;
import com.mobile.responses.ScreeningsResponseV2;
import com.mobile.seats.BringAFriendActivity;
import com.moviepass.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.LinkedList;

import javax.annotation.Nullable;

import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import retrofit2.Call;
import retrofit2.Response;


/**
 * Created by anubis on 6/8/17.
 */

public class TheaterFragment extends MPFragment implements ShowtimeClickListener, MissingCheckinListener {
    public static final String TAG = "found it";
    Theater theaterObject;
    ScreeningsResponseV2 screeningsResponse;
    RecyclerView theaterSelectedRecyclerView;
    ImageView cinemaPin, eTicketingIcon, reserveSeatIcon;
    TextView theaterSelectedAddress, theaterSelectedAddressZip, noTheaters, theaterName;
    LinearLayoutManager theaterSelectedMovieManager;
    TheaterScreeningsAdapter theaterMoviesAdapter;
    EticketTheatersAdapter eticketTheatersAdapter;
    Button buttonCheckIn;
    LinkedList<Screening> moviesAtSelectedTheater;
    ArrayList<String> showtimesAtSelectedTheater;
    View progress;
    Activity myActivity;
    Context myContext;
    Reservation reservation;
    String url;
    @Nullable
    Pair<Screening, String> selected;

    Location currentLocation;

    @Nullable
    Disposable disposable;

    public static final String POLICY = "policy";
    public static final String TOKEN = "token";
    public static final String SCREENING = "screening";
    public static final String SHOWTIME = "showtime";
    public static final String THEATER = "cinema";

    public static TheaterFragment newInstance(Theater theater) {
        Bundle b = new Bundle();
        b.putParcelable(THEATER, Parcels.wrap(Theater.class, theater));
        TheaterFragment f = new TheaterFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fr_theater, container, false);
        ButterKnife.bind(this, rootView);

        //Object & Lists
        theaterObject = Parcels.unwrap(getArguments().getParcelable(THEATER));

        moviesAtSelectedTheater = new LinkedList<>();
        showtimesAtSelectedTheater = new ArrayList<>();
        cinemaPin = rootView.findViewById(R.id.CINEMA_PIN);
        theaterName = rootView.findViewById(R.id.CINEMA_TITLE);
        theaterName.setText(theaterObject.getName());
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

        //Recycler / BringAFriendPagerAdapter / LLM
        int resId = R.anim.layout_anim_bottom;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), resId);
        theaterSelectedRecyclerView = rootView.findViewById(R.id.CINEMA_SELECTED_THEATER_RECYCLER);
        theaterSelectedMovieManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
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
    public void onShowtimeClick(@org.jetbrains.annotations.Nullable Theater theater, @NotNull final Screening screening, @NotNull final String showtime) {
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

        LogUtils.newLog(TAG, "onShowtimeClick: ");

        if (selected != null) {
            fadeIn(buttonCheckIn);
        }

        Availability availability = screening.getAvailability(showtime);
        if (availability == null) {
            return;
        }
        if (availability.isETicket()) {
            buttonCheckIn.setText("Continue to E-Ticketing");
        } else {
            buttonCheckIn.setText("Check In");
        }
        buttonCheckIn.setEnabled(true);
        GoWatchItSingleton.getInstance().userClickedOnShowtime(theaterObject, screening, showtime, String.valueOf(screening.getMoviepassId()), url);
        buttonCheckIn.setOnClickListener(view -> {
            if (isPendingSubscription() && availability.getTicketType() == com.mobile.model.TicketType.E_TICKET) {
                progress.setVisibility(View.VISIBLE);
                reserve(screening, showtime);
            } else if (isPendingSubscription() && availability.getTicketType() == com.mobile.model.TicketType.STANDARD) {
                showActivateCardDialog(screening, showtime);
            } else if (isPendingSubscription() && availability.getTicketType() == com.mobile.model.TicketType.SELECT_SEATING) {
                progress.setVisibility(View.VISIBLE);
                reserve(screening, showtime);
            } else if (availability.getTicketType() == com.mobile.model.TicketType.STANDARD) {
                if (UserPreferences.getProofOfPurchaseRequired() || screening.getPopRequired()) {
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
        if (disposable != null) {
            disposable.dispose();
        }
        disposable = RestClient.getAuthenticated().getScreeningsForTheaterV2(theaterId)
                .subscribe(response -> {
                    screeningsResponse = response;
                    theaterMoviesAdapter.setData(TheaterScreeningsAdapter.Companion.createData(theaterMoviesAdapter.getData(), screeningsResponse.getScreenings(), selected));
                    progress.setVisibility(View.GONE);
                    noTheaters.setVisibility(View.GONE);

                    theaterSelectedRecyclerView.setVisibility(View.VISIBLE);
                }, error -> {
                    error.printStackTrace();
                    progress.setVisibility(View.GONE);
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
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
            Intent intent = BringAFriendActivity.Companion.newIntent(myActivity, theaterObject, screening, time);
            startActivity(intent);
        }
    }

    private void reservationRequest(final Screening screening, TicketInfoRequest checkInRequest, final String showtime) {
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

        intent.putExtra(SCREENING, screeningObject);
        intent.putExtra(SHOWTIME, selectedShowTime);

        startActivity(intent);
    }

    @Override
    public void onClick(@NotNull Screening screening, @NotNull String showTime) {
        onShowtimeClick(null, screening, showTime);
        theaterMoviesAdapter.setData(TheaterScreeningsAdapter.Companion.createData(theaterMoviesAdapter.getData(), screeningsResponse.getScreenings(), selected));
    }
}