package com.moviepass.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.moviepass.R;
import com.moviepass.UserLocationManagerFused;
import com.moviepass.UserPreferences;
import com.moviepass.activities.ConfirmationActivity;
import com.moviepass.activities.SelectSeatActivity;
import com.moviepass.adapters.TheaterMoviesAdapter;
import com.moviepass.listeners.ScreeningPosterClickListener;
import com.moviepass.listeners.ShowtimeClickListener;
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

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by anubis on 6/8/17.
 */

public class TheaterFragment extends Fragment implements ScreeningPosterClickListener, ShowtimeClickListener {
    public static final String TAG = "found it";
    Theater theaterObject;
    ScreeningsResponse screeningsResponse;
    RecyclerView theaterSelectedRecyclerView;
    ImageView backButton, eTicketingIcon, reserveSeatIcon;
    TextView theaterSelectedAddress, theaterSelectedAddressZip;
    LinearLayoutManager theaterSelectedMovieManager;
    TheaterMoviesAdapter theaterMoviesAdapter;
    boolean qualifiersApproved;

    FloatingActionButton fabLoadCard;

    BottomNavigationView bottomNavigationView;
    Screening screening = new Screening();

    ArrayList<Screening> moviesAtSelectedTheater;
    ArrayList<String> showtimesAtSelectedTheater;
    GridLayout showtimeGrid;
    View progress;
    Reservation reservation;

    public static final String TOKEN = "token";
    public static final String SCREENING = "screening";
    public static final String SHOWTIME = "showtime";
    public static final String THEATER = "theater";

    public static TheaterFragment newInstance() {
        return new TheaterFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fr_theater, container, false);
        ButterKnife.bind(this, rootView);

        Bundle extras = getArguments();
        //Object & Lists
        theaterObject = Parcels.unwrap(getActivity().getIntent().getParcelableExtra(THEATER));
        moviesAtSelectedTheater = new ArrayList<>();
        showtimesAtSelectedTheater = new ArrayList<>();


        //ImageViews
        backButton = rootView.findViewById(R.id.CINEMA_BACK);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();

            }
        });

        fabLoadCard = rootView.findViewById(R.id.FAB_LOADCARD);
        progress = rootView.findViewById(R.id.progress);

        eTicketingIcon = rootView.findViewById(R.id.CINEMA_E_TICKETING); //Inivisble by default
        reserveSeatIcon = rootView.findViewById(R.id.CINEMA_RES_SEATS); //invisible by default

        //Textviews
        theaterSelectedAddress = rootView.findViewById(R.id.CINEMA_ADDRESS);
        theaterSelectedAddressZip = rootView.findViewById(R.id.CINEMA_ZIPCITY);
        theaterSelectedAddress.setText(theaterObject.getAddress());
        theaterSelectedAddressZip.setText(theaterObject.getCity() + " " + theaterObject.getState() + " " + theaterObject.getZip());



        /* Start Location Tasks */
        UserLocationManagerFused.getLocationInstance(getContext()).startLocationUpdates();

        //Recycler / Adapter / LLM
        theaterSelectedRecyclerView = rootView.findViewById(R.id.CINEMA_SELECTED_THEATER_RECYCLER);
        theaterSelectedMovieManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        qualifiersApproved = screening.getQualifiersApproved();
        theaterMoviesAdapter = new TheaterMoviesAdapter(getContext(), showtimesAtSelectedTheater, moviesAtSelectedTheater, this, qualifiersApproved);
        theaterSelectedRecyclerView.setLayoutManager(theaterSelectedMovieManager);
        theaterSelectedRecyclerView.setAdapter(theaterMoviesAdapter);
        theaterSelectedRecyclerView.setNestedScrollingEnabled(false);


        loadMovies();

        return rootView;
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
    public void onScreeningPosterClick(int pos, @NotNull Screening screening, @NotNull List<String> startTimes, @NotNull ImageView shareImageView) {
//        TextView showtime;
//
////        showtimesAtSelectedTheater.clear();
////        showtimesAtSelectedTheater.addAll(screeningsResponse.getScreenings());
//        showtimeGrid = getView().findViewById(R.id.SHOWTIMEGRID);
//
//
//        showtimeGrid.setRowCount(2);
//        showtimeGrid.setColumnCount(6);
//        showtimeGrid.removeAllViews();
//
//
//        if (screening.getStartTimes() != null) {
//            for (int i = 0; i < screening.getStartTimes().size(); i++) {
//                showtime = new TextView(getContext());
//                showtime.setText(screening.getStartTimes().get(i));
//                fadeIn(showtime);
//                showtimeGrid.addView(showtime);
//                Log.d(TAG, "showtimes: " + screening.getStartTimes().get(i));
//                showtime.setTextSize(20);
//                showtime.setTextColor(getResources().getColor(R.color.white));
//                showtime.setBackgroundColor(getResources().getColor(R.color.test_blue));
//                showtime.setPadding(10, 5, 10, 5);
//            }
//
//        }
//        Toast.makeText(getContext(), "You clicked" + screening.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShowtimeClick(int pos, @NotNull final Screening screening, @NotNull String showtime) {
        final String time = showtime;


        if (fabLoadCard.getVisibility() == View.GONE) {
            fabLoadCard.setVisibility(View.VISIBLE);
            fadeIn(fabLoadCard);
        } else {
            fabLoadCard.setVisibility(View.GONE);
            fadeOut(fabLoadCard);
        }

        String ticketType = screening.getProvider().ticketType;

        if (ticketType.matches("STANDARD")) {
            String checkIn = "Check In";
        } else if (ticketType.matches("E_TICKET")) {
            String reserve = "Reserve E-Ticket";
        } else {
            String selectSeat = "Select Seat";
        }

        fabLoadCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "toasty", Toast.LENGTH_SHORT).show();
                if (isPendingSubscription()) {
                    showActivateCardDialog(screening, time);
                } else {
                    progress.setVisibility(View.VISIBLE);
                    reserve(screening, time);
                }
            }
        });
    }

    private void loadMovies() {
        int theaterId = theaterObject.getTribuneTheaterId();
        RestClient.getAuthenticated().getScreeningsForTheater(theaterId).enqueue(new Callback<ScreeningsResponse>() {
            @Override
            public void onResponse(Call<ScreeningsResponse> call, Response<ScreeningsResponse> response) {
                screeningsResponse = response.body();
                if (screeningsResponse != null) {
                    moviesAtSelectedTheater.clear();
                    moviesAtSelectedTheater.addAll(screeningsResponse.getScreenings());
                    if (theaterSelectedRecyclerView != null) {
                        theaterSelectedRecyclerView.getRecycledViewPool().clear();
                        theaterMoviesAdapter.notifyDataSetChanged();
                    }
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
        fabLoadCard.setEnabled(false);

        Location mCurrentLocation = UserLocationManagerFused.getLocationInstance(getContext()).mCurrentLocation;
        UserLocationManagerFused.getLocationInstance(getContext()).updateLocation(mCurrentLocation);

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
            Intent intent = new Intent(getContext(), SelectSeatActivity.class);
            intent.putExtra(SCREENING, Parcels.wrap(screening));
            intent.putExtra(SHOWTIME, showtime);
            intent.putExtra(THEATER, Parcels.wrap(theaterObject));
            startActivity(intent);
            getActivity().finish();
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
                        fabLoadCard.setEnabled(true);
                        Toast.makeText(getContext(), jObjError.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    Log.d("resResponse:", "else onResponse:" + "onRespnse fail");
                    progress.setVisibility(View.GONE);
                    fabLoadCard.setEnabled(true);
                }

                fabLoadCard.setEnabled(true);
            }

            @Override
            public void failure(RestError restError) {
                progress.setVisibility(View.GONE);
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
        progress.setVisibility(View.GONE);
        Intent confirmationIntent = new Intent(getContext(), ConfirmationActivity.class);
        confirmationIntent.putExtra(TOKEN, Parcels.wrap(token));
        startActivity(confirmationIntent);
        getActivity().finish();
    }

}