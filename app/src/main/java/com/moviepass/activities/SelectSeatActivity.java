package com.moviepass.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Toast;

import com.moviepass.R;
import com.moviepass.extensions.SeatButton;
import com.moviepass.model.Screening;
import com.moviepass.model.SeatInfo;
import com.moviepass.network.RestClient;
import com.moviepass.requests.PerformanceInfoRequest;
import com.moviepass.responses.SeatingsInfoResponse;

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
    Screening mScreening;
    View mProgress;

    private ArrayList<SeatButton> mSeatButtons;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_seat);

        mScreening = Parcels.unwrap(getIntent().getParcelableExtra(SCREENING));
        String showtime = Parcels.unwrap(getIntent().getParcelableExtra(SHOWTIME));

        mGridSeats = findViewById(R.id.grid_seats);
        mProgress = findViewById(R.id.progress);
//        mTextSeat = ButterKnife.findById(R.id.text_seat);

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

        /* GridLayout.LayoutParams params = (GridLayout.LayoutParams) child.getLayoutParams();
        params.width = (parent.getWidth()/parent.getColumnCount()) -params.rightMargin - params.leftMargin;
        mGridSeats.setLayoutParams(params); */

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

    @Override
    int getContentViewId() {
        return R.layout.activity_browse;
    }

    @Override
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
