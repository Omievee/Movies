package com.moviepass.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.moviepass.R;
import com.moviepass.extensions.SeatButton;
import com.moviepass.helpers.BottomNavigationViewHelper;
import com.moviepass.model.Screening;
import com.moviepass.model.SeatInfo;
import com.moviepass.network.RestClient;
import com.moviepass.requests.PerformanceInfoRequest;
import com.moviepass.responses.SeatingsInfoResponse;
import com.squareup.picasso.Picasso;

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

    public static final String SCREENING = "screening" ;
    public static final String SHOWTIME = "showtime";

    GridLayout mGridSeats;
    ImageView mPoster;
    Screening mScreening;
    TextView mMovieTitle;
    TextView mMovieGenre;
    TextView mMovieRunTime;
    TextView mTheaterName;
    TextView mAuditorium;
    TextView mShowtime;
    TextView mSeats;
    View mProgress;


    private ArrayList<SeatButton> mSeatButtons;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_seat);

        bottomNavigationView = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        mScreening = Parcels.unwrap(getIntent().getParcelableExtra(SCREENING));
        String showtime = Parcels.unwrap(getIntent().getParcelableExtra(SHOWTIME));

        mPoster = findViewById(R.id.poster);
        mMovieTitle = findViewById(R.id.movie_title);
        mMovieGenre = findViewById(R.id.movie_genre);
        mMovieRunTime = findViewById(R.id.text_run_time);
        mTheaterName = findViewById(R.id.theater_name);
        mAuditorium = findViewById(R.id.auditorium);
        mShowtime = findViewById(R.id.showtime);
        mSeats = findViewById(R.id.selected_seats);
        mGridSeats = findViewById(R.id.grid_seats);
        mProgress = findViewById(R.id.progress);

        Picasso.with(this)
                .load(mScreening.getImageUrl())
                .error(R.mipmap.ic_launcher)
                .into(mPoster);

        mMovieTitle.setText(mScreening.getTitle());

        int t = mScreening.getRunningTime();
        int hours = t / 60; //since both are ints, you get an int
        int minutes = t % 60;

        if (mScreening.getRunningTime() == 0) {
            mMovieRunTime.setVisibility(View.GONE);
        } else if (hours > 1) {
            String translatedRunTime = hours + " hours " + minutes + " minutes";
            mMovieRunTime.setText(translatedRunTime);
        } else {
            String translatedRunTime = hours + " hour " + minutes + " minutes";
            mMovieRunTime.setText(translatedRunTime);
        }

        mTheaterName.setText(mScreening.getTheaterName());
        mShowtime.setText(showtime);

        //PerformanceInfo
        int normalizedMovieId = mScreening.getMoviepassId();
        String externalMovieId = mScreening.getProvider().getPerformanceInfo(showtime).getExternalMovieId();
        String format = mScreening.getFormat();
        int tribuneTheaterId = mScreening.getTribuneTheaterId();
        int performanceNumber = mScreening.getProvider().getPerformanceInfo(showtime).getPerformanceNumber();
        String sku = mScreening.getProvider().getPerformanceInfo(showtime).getSku();
        Double price = mScreening.getProvider().getPerformanceInfo(showtime).getPrice();
        String dateTime = mScreening.getProvider().getPerformanceInfo(showtime).getDateTime();
        String auditorium = mScreening.getProvider().getPerformanceInfo(showtime).getAuditorium();
        String performanceId = mScreening.getProvider().getPerformanceInfo(showtime).getPerformanceId();
        String sessionId = mScreening.getProvider().getPerformanceInfo(showtime).getSessionId();
        int theater = mScreening.getProvider().getTheater();

        PerformanceInfoRequest performanceInfoRequest =  new PerformanceInfoRequest(dateTime, externalMovieId, performanceNumber,
                tribuneTheaterId, format, normalizedMovieId, sku, price, auditorium, performanceId, sessionId);


        getSeats(tribuneTheaterId, theater, performanceInfoRequest);
    }

    protected void getSeats(int tribuneTheaterId, int theater, PerformanceInfoRequest performanceInfoRequest) {
        mProgress.setVisibility(View.VISIBLE);

        RestClient.getAuthenticated().getSeats(tribuneTheaterId,
                String.valueOf(theater), performanceInfoRequest).enqueue(
                new Callback<SeatingsInfoResponse>() {
                    @Override
                    public void onResponse(Call<SeatingsInfoResponse> call, Response<SeatingsInfoResponse> response) {
                        mProgress.setVisibility(View.GONE);

                        SeatingsInfoResponse seatingsInfoResponse = response.body();
                        if (seatingsInfoResponse != null) {
                            showSeats(seatingsInfoResponse.seatingInfo.seats, seatingsInfoResponse.seatingInfo.rows, seatingsInfoResponse.seatingInfo.columns);
                        }

                    }

                    @Override
                    public void onFailure(Call<SeatingsInfoResponse> call, Throwable t) {
                        mProgress.setVisibility(View.GONE);
                        Log.d("error", "Unable to download seat information: " + t.getMessage().toString());
                    }
                });
    }

    private void showSeats(List<SeatInfo> seats, int rows, int columns) {
        mGridSeats.setColumnCount(columns);
        mGridSeats.setRowCount(rows);
        mGridSeats.setOrientation(GridLayout.HORIZONTAL);

        mSeatButtons = new ArrayList<>();

        Collections.sort(seats);
        for (SeatInfo seat : seats) {
            SeatButton seatButton = new SeatButton(this, seat);

            final int seatRow = seat.getRow();
            final int seatCol = seat.getColumn();

            seatButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View sender) {
                    Log.d("seat", "seat: " + seatRow + " col:" + seatCol);

                    SeatButton button = (SeatButton) sender;

                    selectSeat(button.getSeatName());
//                    mTextSeat.setText(button.getSeatName());

//                    mSelectedSeat = button.getSeatInfo();

//                    mToken.setSeatName(button.getSeatName());
                }
            });

            seatButton.setPadding(3, 3, 3, 3);
            mGridSeats.addView(seatButton);
            mSeatButtons.add(seatButton);
        }
    }

    private void selectSeat(String seatName) {

        for (SeatButton button : mSeatButtons) {
            button.setSeatSelected(button.getSeatName().matches(seatName));
        }

    }

    /* Bottom Navigation Things */

    int getContentViewId() {
        return R.layout.activity_browse;
    }

    int getNavigationMenuItemId() {
        return R.id.action_browse;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        bottomNavigationView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int itemId = item.getItemId();
                if (itemId == R.id.action_profile) {
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                } else if (itemId == R.id.action_reservations) {
                    Toast.makeText(SelectSeatActivity.this, "E-Ticket Activity", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), ETicketsActivity.class));
                } else if (itemId == R.id.action_browse) {
                } else if (itemId == R.id.action_notifications) {
                    Toast.makeText(SelectSeatActivity.this, "Notification Activity", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), NotificationsActivity.class));
                } else if (itemId == R.id.action_settings) {
                    Toast.makeText(SelectSeatActivity.this, "Settings Activity", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                }
                finish();
            }
        }, 300);
        return true;
    }

    private void updateNavigationBarState(){
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
