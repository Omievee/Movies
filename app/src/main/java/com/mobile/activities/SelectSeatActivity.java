package com.mobile.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.helpshift.support.Log;
import com.mobile.Constants;
import com.mobile.UserLocationManagerFused;
import com.mobile.extensions.SeatButton;
import com.mobile.helpers.BottomNavigationViewHelper;
import com.mobile.helpers.LogUtils;
import com.mobile.model.Movie;
import com.mobile.model.Reservation;
import com.mobile.model.Screening;
import com.mobile.model.ScreeningToken;
import com.mobile.model.SeatInfo;
import com.mobile.model.SeatSelected;
import com.mobile.model.Theater;
import com.mobile.network.RestCallback;
import com.mobile.network.RestClient;
import com.mobile.network.RestError;
import com.mobile.requests.CheckInRequest;
import com.mobile.requests.PerformanceInfoRequest;
import com.mobile.requests.TicketInfoRequest;
import com.mobile.responses.ReservationResponse;
import com.mobile.responses.SeatingsInfoResponse;
import com.moviepass.R;

import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 6/27/17.
 */

public class SelectSeatActivity extends BaseActivity {

    public static final String TAG = "FOUND IT";

    public static final String MOVIE = "movie";
    public static final String SCREENING = "screening";
    public static final String SHOWTIME = "showtime";
    public static final String THEATER = "theater";
    public static final String TOKEN = "token";
    public static final String SEAT = "seat";

    LocationUpdateBroadCast mLocationBroadCast;
    boolean mLocationAcquired;
    private Location mMyLocation;

    View coordinatorLayout;
    GridLayout mGridSeatsA, mGridSeatsB, mGridSeatsC, mGridSeatsD,
            mGridSeatsE, mGridSeatsF, mGridSeatsG, mGridSeatsH, mGridSeatsI,
            mGridSeatsJ, mGridSeatsK, mGridSeatsL, mGridSeatsM;
    ImageView mMoviePoster;
    ImageView onBackButton;
    Screening screeningObject;
    Theater theaterObject;
    TextView mSelectedMovieTitle;
    TextView mMovieRunTime;
    TextView mTheaterSelected;
    TextView mScreeningShowtime;
    TextView mSelectedSeat;
    Button reserveSeatButton;
    View mProgressWheel;
    String selectedShowTime;
    String mProviderName;
    TicketInfoRequest mTicketRequest;
    CheckInRequest mCheckinRequest;
    PerformanceInfoRequest mPerformReq;
    Movie movieObject;
    boolean isSeatSelected = false;
    private ArrayList<SeatButton> mSeatButtons;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_select_seat);

        bottomNavigationView = findViewById(R.id.SEATCHART_NAV);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);


        Intent intent = getIntent();
        screeningObject = Parcels.unwrap(intent.getParcelableExtra(SCREENING));
        selectedShowTime = getIntent().getStringExtra(SHOWTIME);
        movieObject = Parcels.unwrap(intent.getParcelableExtra(MOVIE));
//        screeningObject = Parcels.unwrap(intent.getParcelableExtra(SCREENING));

        coordinatorLayout = findViewById(R.id.mCoordinator);
        mSelectedMovieTitle = findViewById(R.id.SEATCHART_MOVIETITLE);
        mTheaterSelected = findViewById(R.id.SEATCHART_THEATER);
        mScreeningShowtime = findViewById(R.id.SEATCHART_SHOWTIME);
        mSelectedSeat = findViewById(R.id.SEATCHART_SEAT);
        onBackButton = findViewById(R.id.SEATCHART_ONBACK);
        mGridSeatsA = findViewById(R.id.gridSeatsA);
        mGridSeatsB = findViewById(R.id.gridSeatsB);
        mGridSeatsC = findViewById(R.id.gridSeatsC);
        mGridSeatsD = findViewById(R.id.gridSeatsD);
        mGridSeatsE = findViewById(R.id.gridSeatsE);
        mGridSeatsF = findViewById(R.id.gridSeatsF);
        mGridSeatsG = findViewById(R.id.gridSeatsG);
        mGridSeatsH = findViewById(R.id.gridSeatsH);
        mGridSeatsI = findViewById(R.id.gridSeatsI);
        mGridSeatsJ = findViewById(R.id.gridSeatsJ);
        mGridSeatsK = findViewById(R.id.gridSeatsK);
        mGridSeatsL = findViewById(R.id.gridSeatsL);
        mGridSeatsM = findViewById(R.id.gridSeatsM);

        reserveSeatButton = findViewById(R.id.SEATCHART_RESERVE);
        mProgressWheel = findViewById(R.id.progress);


        if (screeningObject != null) {
            mSelectedMovieTitle.setText(screeningObject.getTitle());
            mTheaterSelected.setText(screeningObject.getTheaterName());
        }

        mScreeningShowtime.setText(selectedShowTime);


//        reserveSeatButton.setText(R.string.activity_select_seat_activity_title);

        //PerformanceInfo

        LogUtils.newLog(TAG, "onCreate: " + screeningObject.getProvider().getProviderName());
        checkProviderDoPerformanceInfoRequest();
        //If seat hasn't been selected return error

        reserveSeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSeatSelected)
                    makeSnackbar(getString(R.string.activity_select_seat_select_first));
            }
        });

        onBackButton.setOnClickListener(v -> SelectSeatActivity.super.onBackPressed());
    }


    protected PerformanceInfoRequest checkProviderDoPerformanceInfoRequest() {
        if (screeningObject.getProvider().getProviderName().equalsIgnoreCase("MOVIEXCHANGE")) {
            int normalizedMovieId = screeningObject.getMoviepassId();
            String externalMovieId = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getExternalMovieId();
            String format = screeningObject.getFormat();
            int tribuneTheaterId = screeningObject.getTribuneTheaterId();
            int screeningId = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getScreeningId();
            int performanceNumber = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getPerformanceNumber();
            String sku = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getSku();
            Double price = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getPrice();
            String dateTime = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getDateTime();
            String auditorium = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getAuditorium();
            String performanceId = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getPerformanceId();
            String sessionId = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getSessionId();
            int theater = screeningObject.getProvider().getTheater();
            String cinemaChainId = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getCinemaChainId();
            String showtimeId = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getShowtimeId();
            TicketType ticketType = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getTicketType();


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
            getSeats(tribuneTheaterId, theater, mPerformReq);

            return mPerformReq;


        } else {
            //IF not movieXchange then it will simply request these parameters:
            int normalizedMovieId = screeningObject.getMoviepassId();
            String externalMovieId = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getExternalMovieId();
            String format = screeningObject.getFormat();
            int tribuneTheaterId = screeningObject.getTribuneTheaterId();
            int performanceNumber = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getPerformanceNumber();
            String sku = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getSku();
            Double price = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getPrice();
            String dateTime = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getDateTime();
            String auditorium = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getAuditorium();
            String performanceId = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getPerformanceId();
            String sessionId = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getSessionId();
            int theater = screeningObject.getProvider().getTheater();

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

            getSeats(tribuneTheaterId, theater, mPerformReq);

            return mPerformReq;
        }

    }


    protected void getSeats(final int tribuneTheaterId, final int theater, PerformanceInfoRequest performanceInfoRequest) {
        mProgressWheel.setVisibility(View.VISIBLE);

        RestClient.getAuthenticated().getSeats(tribuneTheaterId, String.valueOf(theater), performanceInfoRequest).enqueue(new Callback<SeatingsInfoResponse>() {
            @Override
            public void onResponse(Call<SeatingsInfoResponse> call, Response<SeatingsInfoResponse> response) {
                mProgressWheel.setVisibility(View.GONE);
                SeatingsInfoResponse seatingsInfoResponse = response.body();
                if (seatingsInfoResponse != null) {
                    showSeats(
                            seatingsInfoResponse.seatingInfo.seats,
                            seatingsInfoResponse.seatingInfo.rows,
                            seatingsInfoResponse.seatingInfo.columns);
                }
            }

            @Override
            public void onFailure(Call<SeatingsInfoResponse> call, Throwable t) {
                mProgressWheel.setVisibility(View.GONE);
            }
        });
    }

    private void showSeats(List<SeatInfo> seats, int rows, int columns) {
        mGridSeatsA.setBackgroundColor(Color.TRANSPARENT);
        mGridSeatsA.setColumnCount(columns);
        mGridSeatsA.setRowCount(rows);
        mSeatButtons = new ArrayList<>();
        int seatPadding = (int) getResources().getDimension(R.dimen.seat_padding);
        Collections.sort(seats);
        for (SeatInfo seat : seats) {
            SeatButton seatButton = new SeatButton(this, seat);
            //Check if Moviexchange or Radian to populat proper seating.. if not. business as usual.
            if ((screeningObject.getProvider().getProviderName().equalsIgnoreCase("MOVIEXCHANGE")) ||
                    (screeningObject.getProvider().getProviderName().equalsIgnoreCase("RADIANT"))) {
                GridLayout gridLayout = null;
                if (seat.getSeatName().contains("A")) {
                    gridLayout = mGridSeatsA;
                }
                if (seat.getSeatName().contains("B")) {
                    gridLayout = mGridSeatsB;
                }
                if (seat.getSeatName().contains("C")) {
                    gridLayout = mGridSeatsC;
                }
                if (seat.getSeatName().contains("D")) {
                    gridLayout = mGridSeatsD;
                }
                if (seat.getSeatName().contains("E")) {
                    gridLayout = mGridSeatsE;
                }
                if (seat.getSeatName().contains("F")) {
                    gridLayout = mGridSeatsF;
                }
                if (seat.getSeatName().contains("G")) {
                    gridLayout = mGridSeatsG;
                }
                if (seat.getSeatName().contains("H")) {
                    gridLayout = mGridSeatsH;
                }
                if (seat.getSeatName().contains("I")) {
                    gridLayout = mGridSeatsI;
                }
                if (seat.getSeatName().contains("J")) {
                    gridLayout = mGridSeatsJ;
                }
                if (seat.getSeatName().contains("K")) {
                    gridLayout = mGridSeatsK;
                }
                if (seat.getSeatName().contains("L")) {
                    gridLayout = mGridSeatsL;
                }

                if (seat.getSeatName().contains("M")) {
                    gridLayout = mGridSeatsM;
                }
                if(gridLayout != null) {
                    GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                    lp.setGravity(Gravity.CENTER_HORIZONTAL);
                    gridLayout.addView(seatButton, lp);
                }
                seatButton.setPadding(seatPadding, seatPadding, seatPadding, seatPadding);
                mSeatButtons.add(seatButton);

            } else {
                seatButton.setPadding(seatPadding, seatPadding, seatPadding, seatPadding);
                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setGravity(Gravity.CENTER_HORIZONTAL);
                mGridSeatsA.addView(seatButton, lp);
                mSeatButtons.add(seatButton);
            }

            seatButton.setOnClickListener(sender -> {
                final SeatButton seatBtn = (SeatButton) sender;
                final SeatInfo seatInfo = seatBtn.getSeatInfo();
                if (seatInfo.isWheelChairOrCompanion()) {
                    final @StringRes int message;
                    final @StringRes int title;
                    if(seatInfo.getSeatType()== SeatInfo.SeatType.SeatTypeWheelchair) {
                        title = R.string.dialog_select_seat_wheelchair_title;
                        message = R.string.dialog_select_seat_wheelchair_message;
                    } else {
                        title = R.string.dialog_select_seat_companion_title;
                        message = R.string.dialog_select_seat_companion_message;
                    }
                    new AlertDialog.Builder(SelectSeatActivity.this)
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onSeatClicked(seatBtn, seatInfo);
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .create()
                            .show();
                } else {
                    onSeatClicked(seatBtn, seatInfo);
                }
            });

        }
    }

    private void onSeatClicked(SeatButton button, SeatInfo seat) {
        final int seatRow = seat.getRow();
        final int seatCol = seat.getColumn();
        final String finalSeatName = seat.getSeatName();
        if (finalSeatName != null) {
            mSelectedSeat.setText(finalSeatName);
        } else {
            String formattedSeatName = "Row: " + seatCol + " Seat: " + seatRow;
            mSelectedSeat.setText(formattedSeatName);
        }

        selectSeat(button.getSeatInfo());
        reserveSeatButton.setOnClickListener(view -> {

                    LogUtils.newLog(TAG, "button name:" + button.getSeatName());
                    LogUtils.newLog(TAG, "button row: " + button.getSeatInfo().getRow());
                    LogUtils.newLog(TAG, "button col: " + button.getSeatInfo().getColumn());


            SeatSelected seatSelected = new SeatSelected(button.getSeatInfo().getRow(), button.getSeatInfo().getColumn(), button.getSeatName());

            Intent intent = new Intent(SelectSeatActivity.this, EticketConfirmation.class);

            intent.putExtra(SCREENING, Parcels.wrap(screeningObject));
            intent.putExtra(SHOWTIME, selectedShowTime);
            intent.putExtra(SEAT, Parcels.wrap(seatSelected));

            startActivity(intent);


//TODO: come back to this..
//                            SeatSelected seatObject = new SeatSelected(button.getSeatInfo().getRow(), button.getSeatInfo().getColumn(), button.getSeatName());
//                            reserveWithSeat(screeningObject, selectedShowTime, seatObject);
//                    mProgressWheel.setVisibility(View.VISIBLE);
        });
    }

    private void selectSeat(SeatInfo seatInfo) {
        for (SeatButton button : mSeatButtons) {
            button.setSeatSelected(button.getSeatInfo().equals(seatInfo));
            reserveSeatButton.setText(R.string.activity_select_seat_reserve);
            isSeatSelected = true;
        }
    }
//
//    private void reserve(Screening screening, String showtime, SeatSelected selectedSeat) {
//
//        Location mCurrentLocation = UserLocationManagerFused.getLocationInstance(this).mCurrentLocation;
//        UserLocationManagerFused.getLocationInstance(this).updateLocation(mCurrentLocation);
//        SelectedSeat selectedSeatRequest = new SelectedSeat(selectedSeat.getSelectedSeatRow(), selectedSeat.getSelectedSeatColumn());
//
//
//        mProviderName = screening.getProvider().providerName;
//        mTicketRequest = new TicketInfoRequest(checkProviderDoPerformanceInfoRequest(), selectedSeatRequest);
//        mCheckinRequest = new CheckInRequest(mTicketRequest, mProviderName, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
//        reservationRequest(screening, mCheckinRequest, showtime, selectedSeat);
//
//    }


    //TODO:
    private void reservationRequest(final Screening screening, CheckInRequest checkInRequest, final String showtime, final SeatSelected seatSelected) {
        RestClient.getAuthenticated().checkIn(checkInRequest).enqueue(new RestCallback<ReservationResponse>() {
            @Override
            public void onResponse(Call<ReservationResponse> call, Response<ReservationResponse> response) {
                ReservationResponse reservationResponse = response.body();

                if (reservationResponse != null && reservationResponse.isOk()) {
                    reserveSeatButton.setEnabled(true);
                    mProgressWheel.setVisibility(View.GONE);
                    Reservation reservation = reservationResponse.getReservation();
                    String confirmationCode = reservationResponse.getE_ticket_confirmation().getConfirmationCode();
                    String qrUrl = reservationResponse.getE_ticket_confirmation().getBarCodeUrl();

                    ScreeningToken token = new ScreeningToken(screening, showtime, reservation, qrUrl, confirmationCode, seatSelected);

                    showConfirmation(token);

                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());

                        Toast.makeText(SelectSeatActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        mProgressWheel.setVisibility(View.GONE);
                        reserveSeatButton.setEnabled(true);
                    } catch (Exception e) {
                        Toast.makeText(SelectSeatActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        mProgressWheel.setVisibility(View.GONE);
                        reserveSeatButton.setEnabled(true);
                    }
                }

            }

            @Override
            public void failure(RestError restError) {
                mProgressWheel.setVisibility(View.GONE);
                reserveSeatButton.setEnabled(true);

                String hostname = "Unable to resolve host: No address associated with hostname";

                if (restError != null && restError.getMessage() != null && restError.getMessage().toLowerCase().contains("none.get")) {
                    Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_LONG).show();
                }
                if (restError != null && restError.getMessage() != null && restError.getMessage().toLowerCase().contains(hostname.toLowerCase())) {
                    Toast.makeText(getApplicationContext(), R.string.data_connection, Toast.LENGTH_LONG).show();
                }
                if (restError != null && restError.getMessage() != null && restError.getMessage().toLowerCase().matches("You have a pending reservation")) {
                    Toast.makeText(getApplicationContext(), R.string.pending_reservation, Toast.LENGTH_LONG).show();
                } else if (restError != null) {
                    Toast.makeText(getApplicationContext(), restError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showConfirmation(ScreeningToken token) {
        Intent confirmationIntent = new Intent(SelectSeatActivity.this, ConfirmationActivity.class);
        confirmationIntent.putExtra(TOKEN, Parcels.wrap(token));
        startActivity(confirmationIntent);
        finish();
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
            UserLocationManagerFused.getLocationInstance(SelectSeatActivity.this).stopLocationUpdates();
            onLocationChanged(UserLocationManagerFused.getLocationInstance(context).mCurrentLocation);
        }
    }

    protected void onLocationChanged(Location location) {
        UserLocationManagerFused.getLocationInstance(this).stopLocationUpdates();

        if (location != null) {
            UserLocationManagerFused.getLocationInstance(this).updateLocation(location);

            mMyLocation = location;

            mLocationAcquired = true;
        }
    }

    public void currentLocationTasks() {
        registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));
        UserLocationManagerFused.getLocationInstance(SelectSeatActivity.this).startLocationUpdates();
        mLocationAcquired = false;

        boolean enabled = UserLocationManagerFused.getLocationInstance(SelectSeatActivity.this).isLocationEnabled();
        if (!enabled) {
//            showDialogGPS();
        } else {
            Location location = UserLocationManagerFused.getLocationInstance(SelectSeatActivity.this).mCurrentLocation;
            onLocationChanged(location);

            if (location != null) {
                UserLocationManagerFused.getLocationInstance(this).requestLocationForCoords(location.getLatitude(), location.getLongitude(), SelectSeatActivity.this);
            }
        }
    }

    public void makeSnackbar(String message) {
        final Snackbar snackbar = Snackbar.make(findViewById(R.id.mCoordinator), message, Snackbar.LENGTH_SHORT);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackbar.getView().getLayoutParams();
        snackbar.getView().setLayoutParams(params);
        snackbar.show();


    }

    @Override
    public void onBackPressed() {
        if (getIntent().getParcelableExtra(THEATER) != null) {
            Theater theater = Parcels.unwrap(getIntent().getParcelableExtra(THEATER));
            Intent intent = new Intent(SelectSeatActivity.this, TheaterActivity.class);
            intent.putExtra(TheaterActivity.THEATER, Parcels.wrap(theater));
            startActivity(intent);
            finish();
        } else if (getIntent().getParcelableExtra(MOVIE) != null) {
            Movie movie = Parcels.unwrap(getIntent().getParcelableExtra(MOVIE));
            Intent intent = new Intent(SelectSeatActivity.this, MovieActivity.class);
            intent.putExtra(MOVIE, Parcels.wrap(movie));
            startActivity(intent);
            finish();
        } else {
            finish();

        }
    }

    /* Bottom Navigation Things */
    int getContentViewId() {
        return R.layout.activity_browse;
    }

    int getNavigationMenuItemId() {
        return R.id.action_movies;
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


}