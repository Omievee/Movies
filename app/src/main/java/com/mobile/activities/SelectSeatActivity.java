package com.mobile.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import com.mobile.UserLocationManagerFused;
import com.mobile.extensions.SeatButton;
import com.mobile.helpers.LogUtils;
import com.mobile.model.Movie;
import com.mobile.model.Screening;
import com.mobile.model.SeatInfo;
import com.mobile.model.SeatSelected;
import com.mobile.model.Theater;
import com.mobile.network.RestClient;
import com.mobile.requests.PerformanceInfoRequest;
import com.mobile.responses.SeatingsInfoResponse;
import com.moviepass.R;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 6/27/17.
 */

public class SelectSeatActivity extends AppCompatActivity {

    public static final String TAG = "FOUND IT";

    public static final String MOVIE = "movie";
    public static final String SCREENING = "screening";
    public static final String SHOWTIME = "showtime";
    public static final String THEATER = "theater";
    public static final String TOKEN = "token";
    public static final String SEAT = "seat";

    LocationUpdateBroadCast mLocationBroadCast;
    boolean mLocationAcquired;

    View coordinatorLayout;
    GridLayout mGridSeatsA;
    ImageView onBackButton;
    Screening screeningObject;
    TextView mSelectedMovieTitle;
    TextView mTheaterSelected;
    TextView mScreeningShowtime;
    TextView mSelectedSeat;
    Button reserveSeatButton;
    View mProgressWheel;
    String selectedShowTime;
    PerformanceInfoRequest mPerformReq;
    Movie movieObject;
    boolean isSeatSelected = false;
    private Location mMyLocation;
    private ArrayList<SeatButton> mSeatButtons;
    private Theater theater;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_select_seat);

        Intent intent = getIntent();
        screeningObject = Parcels.unwrap(intent.getParcelableExtra(SCREENING));
        selectedShowTime = getIntent().getStringExtra(SHOWTIME);
        movieObject = Parcels.unwrap(intent.getParcelableExtra(MOVIE));
        theater = Parcels.unwrap(intent.getParcelableExtra(THEATER));
//        screeningObject = Parcels.unwrap(intent.getParcelableExtra(SCREENING));

        coordinatorLayout = findViewById(R.id.mCoordinator);
        mSelectedMovieTitle = findViewById(R.id.SEATCHART_MOVIETITLE);
        mTheaterSelected = findViewById(R.id.SEATCHART_THEATER);
        mScreeningShowtime = findViewById(R.id.SEATCHART_SHOWTIME);
        mSelectedSeat = findViewById(R.id.SEATCHART_SEAT);
        onBackButton = findViewById(R.id.SEATCHART_ONBACK);
        mGridSeatsA = findViewById(R.id.gridSeatsA);

        reserveSeatButton = findViewById(R.id.SEATCHART_RESERVE);
        mProgressWheel = findViewById(R.id.progress);


        if (screeningObject != null) {
            mSelectedMovieTitle.setText(screeningObject.getTitle());
            mTheaterSelected.setText(screeningObject.getTheaterName());
        }

        mScreeningShowtime.setText(selectedShowTime);

        LogUtils.newLog(TAG, "onCreate: " + screeningObject.getProvider().getProviderName());
        checkProviderDoPerformanceInfoRequest();
        //If seat hasn't been selected return error

        reserveSeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSeatSelected)
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
        SeatInfo[][] seatGrid = new SeatInfo[rows][columns];
        for (SeatInfo seat : seats) {
            seatGrid[seat.getRow() - 1][seat.getColumn() - 1] = seat;
        }
        mGridSeatsA.setBackgroundColor(Color.TRANSPARENT);
        mGridSeatsA.setColumnCount(columns);
        mGridSeatsA.setRowCount(rows);
        mGridSeatsA.setAlignmentMode(GridLayout.HORIZONTAL);
        mSeatButtons = new ArrayList<>();
        int seatPadding = (int) getResources().getDimension(R.dimen.seat_padding);
        for (int row = 0; row < seatGrid.length; row++) {
            for (int column = 0; column < seatGrid[row].length; column++) {
                SeatInfo seat = seatGrid[row][column];
                GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                param.setGravity(Gravity.CENTER);
                param.rowSpec = GridLayout.spec(row, GridLayout.CENTER);
                param.columnSpec = GridLayout.spec(column, GridLayout.CENTER);
                if (seat == null) {
                    ImageView empty = new ImageView(this);
                    empty.setImageResource(R.drawable.empty_seat);
                    mGridSeatsA.addView(empty, param);
                } else {
                    SeatButton seatButton = new SeatButton(this, seat);
                    mSeatButtons.add(seatButton);
                    mGridSeatsA.addView(seatButton, param);
                    seatButton.setPadding(seatPadding, seatPadding, seatPadding, seatPadding);
                    seatButton.setOnClickListener(sender -> {
                        final SeatButton seatBtn = (SeatButton) sender;
                        final SeatInfo seatInfo = seatBtn.getSeatInfo();
                        if (seatInfo.isWheelChairOrCompanion()) {
                            final @StringRes int message;
                            final @StringRes int title;
                            if (seatInfo.getSeatType() == SeatInfo.SeatType.SeatTypeWheelchair) {
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
            intent.putExtra(THEATER, Parcels.wrap(theater));

            startActivity(intent);

        });
    }

    private void selectSeat(SeatInfo seatInfo) {
        for (SeatButton button : mSeatButtons) {
            button.setSeatSelected(button.getSeatInfo().equals(seatInfo));
            reserveSeatButton.setText(R.string.activity_select_seat_reserve);
            isSeatSelected = true;
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
}