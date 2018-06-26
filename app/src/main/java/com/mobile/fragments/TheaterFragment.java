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
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.mobile.ApiError;
import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.activities.ConfirmationActivity;
import com.mobile.adapters.MissingCheckinListener;
import com.mobile.adapters.TheaterScreeningsAdapter;
import com.mobile.helpers.GoWatchItSingleton;
import com.mobile.helpers.LogUtils;
import com.mobile.history.HistoryManager;
import com.mobile.history.model.ReservationHistory;
import com.mobile.listeners.ShowtimeClickListener;
import com.mobile.location.UserLocation;
import com.mobile.model.Availability;
import com.mobile.model.Reservation;
import com.mobile.model.Screening;
import com.mobile.model.ScreeningToken;
import com.mobile.model.Theater;
import com.mobile.network.Api;
import com.mobile.network.RestClient;
import com.mobile.recycler.decorator.SpaceDecorator;
import com.mobile.requests.CardActivationRequest;
import com.mobile.requests.TicketInfoRequest;
import com.mobile.reservation.ReservationActivity;
import com.mobile.responses.CardActivationResponse;
import com.mobile.responses.ScreeningsResponseV2;
import com.mobile.seats.BringAFriendActivity;
import com.moviepass.R;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import retrofit2.Call;
import retrofit2.Response;


public class TheaterFragment extends MPFragment implements ShowtimeClickListener, MissingCheckinListener {
    public static final String TAG = "found it";
    Theater selectedTheaterObject;

    @Inject
    com.mobile.location.LocationManager locationManager;

    @Inject
    Api api;

    @Inject
    HistoryManager historyManager;

    Pair<List<ReservationHistory>, ScreeningsResponseV2> screeningsResponse;
    RecyclerView selectedTheaterRecyclerView;
    ImageView pinIcon, eticketIcon, reserveSeatIcon;
    TextView theaterSelectedAddress, selectedTheaterCity, noTheaters, selectedTheaterName;
    LinearLayoutManager theaterSelectedMovieManager;
    TheaterScreeningsAdapter theaterMoviesAdapter;
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

    Location currentLocation = new Location("");


    @Nullable
    Disposable disposable;

    public static final String POLICY = "policy";
    public static final String TOKEN = "token";
    public static final String SCREENING = "screening";
    public static final String SHOWTIME = "showtime";
    public static final String THEATER = "cinema";

    public static TheaterFragment newInstance(Theater theater) {
        Bundle b = new Bundle();
        b.putParcelable(Constants.THEATER, Parcels.wrap(Theater.class, theater));
        TheaterFragment f = new TheaterFragment();
        f.setArguments(b);
        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedTheaterObject = Parcels.unwrap(getArguments().getParcelable(Constants.THEATER));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fr_theater, container, false);
        ButterKnife.bind(this, rootView);

        //Object & Lists
        moviesAtSelectedTheater = new LinkedList<>();
        showtimesAtSelectedTheater = new ArrayList<>();


        pinIcon = rootView.findViewById(R.id.pinIcon);
        selectedTheaterName = rootView.findViewById(R.id.selectedTheaterName);
        selectedTheaterName.setText(selectedTheaterObject.getName());
        buttonCheckIn = rootView.findViewById(R.id.button_check_in);
        progress = rootView.findViewById(R.id.selectedTheaterProgress);
        progress.setVisibility(View.VISIBLE);
        eticketIcon = rootView.findViewById(R.id.eticketIcon);
        reserveSeatIcon = rootView.findViewById(R.id.selectSeatIcon);

        if (selectedTheaterObject.ticketTypeIsStandard()) {
            eticketIcon.setVisibility(View.INVISIBLE);
            reserveSeatIcon.setVisibility(View.INVISIBLE);
        } else if (selectedTheaterObject.ticketTypeIsETicket()) {
            reserveSeatIcon.setVisibility(View.INVISIBLE);
        } else if (selectedTheaterObject.ticketTypeIsSelectSeating()) {
            reserveSeatIcon.setVisibility(View.INVISIBLE);
        }
        //Textviews
        theaterSelectedAddress = rootView.findViewById(R.id.selectedTheaterAddress);
        selectedTheaterCity = rootView.findViewById(R.id.selectedTheaterCity);
        theaterSelectedAddress.setText(selectedTheaterObject.getAddress());
        selectedTheaterCity.setText(selectedTheaterObject.getCity() + " " + selectedTheaterObject.getState() + " " + selectedTheaterObject.getZip());
        noTheaters = rootView.findViewById(R.id.NoTheaters);
        final Uri uri = Uri.parse("geo:" + selectedTheaterObject.getLat() + "," + selectedTheaterObject.getLon() + "?q=" + Uri.encode(selectedTheaterObject.getName()));

        pinIcon.setOnClickListener(v -> {
            try {
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(uri)));
                mapIntent.setPackage("com.google.android.apps.maps");
                myContext.startActivity(mapIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(myContext, "Google Maps isn't installed", Toast.LENGTH_SHORT).show();
            } catch (Exception x) {
                x.getMessage();
            }
        });

        /* Start Location Tasks */

        //Recycler / BringAFriendPagerAdapter / LLM
        int resId = R.anim.layout_anim_bottom;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), resId);
        selectedTheaterRecyclerView = rootView.findViewById(R.id.selectedTheaterRecyclerView);
        theaterSelectedMovieManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        theaterMoviesAdapter = new TheaterScreeningsAdapter(this, this);
        selectedTheaterRecyclerView.setLayoutManager(theaterSelectedMovieManager);
        selectedTheaterRecyclerView.setAdapter(theaterMoviesAdapter);
        selectedTheaterRecyclerView.setLayoutAnimation(animation);
        selectedTheaterRecyclerView.addItemDecoration(new SpaceDecorator(null, null, null, null, null, 300));

        loadMovies();

        if (selectedTheaterObject.getName().contains("Flix Brewhouse")) {
            String theater = selectedTheaterObject.getName();
            Bundle bundle = new Bundle();
            bundle.putString(POLICY, theater);

            TheaterPolicy fragobj = new TheaterPolicy();
            fragobj.setArguments(bundle);
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fragobj.show(fm, "fr_theaterpolicy");
        }

        url = "https://moviepass.com/go/theaters/" + selectedTheaterObject.getId();
        if (!GoWatchItSingleton.getInstance().getCampaign().equalsIgnoreCase("no_campaign"))
            url = url + "/" + GoWatchItSingleton.getInstance().getCampaign();


        GoWatchItSingleton.getInstance().userOpenedTheater(selectedTheaterObject, url);

        return rootView;
    }


    @Override
    public void onViewCreated(@NotNull View view, @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        UserLocation last = locationManager.lastLocation();
        if (last != null) {
            currentLocation.setLatitude(last.getLat());
            currentLocation.setLongitude(last.getLon());
        }
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
        myContext = context;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private Parcelable state;

    @Override
    public void onPause() {
        super.onPause();
        state = theaterSelectedMovieManager.onSaveInstanceState();
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
        theaterMoviesAdapter.setData(TheaterScreeningsAdapter.Companion.createData(theaterMoviesAdapter.getData(), screeningsResponse, null, selected));
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
        GoWatchItSingleton.getInstance().userClickedOnShowtime(selectedTheaterObject, screening, showtime, String.valueOf(screening.getMoviepassId()), url);
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
        int theaterId = selectedTheaterObject.getTribuneTheaterId();
        if (disposable != null) {
            disposable.dispose();
        }
        disposable = Observable.zip(historyManager.getHistory(),
                api.getScreeningsForTheaterV2(theaterId).toObservable(),
                (history, screeningsResponse) -> new Pair<>(history, screeningsResponse))
                .subscribe(response -> {
                    screeningsResponse = response;
                    theaterMoviesAdapter.setData(TheaterScreeningsAdapter.Companion.createData(theaterMoviesAdapter.getData(), screeningsResponse, null, selected));
                    progress.setVisibility(View.GONE);
                    noTheaters.setVisibility(View.GONE);
                    selectedTheaterRecyclerView.setVisibility(View.VISIBLE);
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
        if (fetchLocationSub != null) {
            fetchLocationSub.dispose();
        }
        if (reserveSub != null) {
            reserveSub.dispose();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        buttonCheckIn.setEnabled(true);
        fetchLocation();
        if (state != null) {
            theaterSelectedMovieManager.onRestoreInstanceState(state);
        }
    }

    @Nullable
    Disposable fetchLocationSub;

    private void fetchLocation() {
        if (fetchLocationSub != null) {
            fetchLocationSub.dispose();
        }
        fetchLocationSub = locationManager.location().subscribe(v -> {
            currentLocation.setLatitude(v.getLat());
            currentLocation.setLongitude(v.getLon());
        }, e -> {

        });
    }

    public void reserve(Screening screening, String showtime) {
        Screening screen = screening;
        String time = showtime;


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
            if (currentLocation != null) {
                TicketInfoRequest checkInRequest = new TicketInfoRequest(availability.getProviderInfo(), null, null, currentLocation.getLatitude(), currentLocation.getLongitude());
                reservationRequest(screen, checkInRequest, time);
            } else {
                Toast.makeText(myActivity, "Enable GPS to check in.", Toast.LENGTH_SHORT).show();
            }
        } else {
            progress.setVisibility(View.GONE);
            Intent intent = BringAFriendActivity.Companion.newIntent(myContext, selectedTheaterObject, screening, time);
            startActivity(intent);
        }
    }

    @Nullable
    Disposable reserveSub;

    private void reservationRequest(final Screening screening, TicketInfoRequest checkInRequest, final String showtime) {
        if (reserveSub != null) {
            reserveSub.dispose();
        }
        reserveSub = api.reserve(checkInRequest)
                .doAfterTerminate(() -> progress.setVisibility(View.GONE))
                .subscribe(res -> {
                    reservation = res.getReservation();
                    UserPreferences.saveReservation(new ScreeningToken(screening, res.getShowtime(), reservation, selectedTheaterObject));
                    if (res.getETicketConfirmation() != null) {

                        ScreeningToken token = new ScreeningToken(screening, showtime, reservation, res.getETicketConfirmation(), selectedTheaterObject);
                        showConfirmation(token);
                        GoWatchItSingleton.getInstance().checkInEvent(selectedTheaterObject, screening, showtime, "ticket_purchase", String.valueOf(selectedTheaterObject.getId()), url);

                    } else {
                        ScreeningToken token = new ScreeningToken(screening, showtime, reservation, selectedTheaterObject);
                        showConfirmation(token);
                        GoWatchItSingleton.getInstance().checkInEvent(selectedTheaterObject, screening, showtime, "ticket_purchase", String.valueOf(selectedTheaterObject.getId()), url);
                    }
                }, err -> {
                    if (err instanceof ApiError) {
                        ApiError error = (ApiError) err;
                        Toast.makeText(myContext, error.getError().getMessage(), Toast.LENGTH_LONG).show();
                    }
                    buttonCheckIn.setVisibility(View.VISIBLE);
                    buttonCheckIn.setEnabled(true);
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
        alert.setNegativeButton("Activate Later", (dialog, which) -> {
            Toast.makeText(getContext(), R.string.dialog_activate_card_must_activate_standard_theater, Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });
        alert.show();
    }

    private void showConfirmation(ScreeningToken token) {
        startActivity(ReservationActivity.Companion.newInstance(myContext, token, true));
        Activity activity = getActivity();
        if (activity != null) {
            activity.onBackPressed();
        }
    }

    @Override
    public void onClick(@NotNull Screening screening, @NotNull String showTime) {
        onShowtimeClick(null, screening, showTime);
        int pos = selectedTheaterRecyclerView.getScrollY();
        theaterMoviesAdapter.setData(TheaterScreeningsAdapter.Companion.createData(theaterMoviesAdapter.getData(), screeningsResponse, null, selected));
    }
}