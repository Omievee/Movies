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
import com.moviepass.model.Movie;
import com.moviepass.model.Screening;
import com.moviepass.model.SeatInfo;
import com.moviepass.model.Theater;
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
    public static final String THEATER = "theater";
    public static final String MOVIE = "movie";


    GridLayout gridSeats;
    ImageView poster;
    Screening screening;
    TextView movieTitle;
    TextView movieRunTime;
    TextView theaterName;
    TextView screeningShowtime;
    TextView selectedSeat;
    View progress;

    private ArrayList<SeatButton> seatButtons;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_seat);

        bottomNavigationView = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        screening = Parcels.unwrap(getIntent().getParcelableExtra(SCREENING));
        String showtime = getIntent().getStringExtra(SHOWTIME);

        poster = findViewById(R.id.poster);
        movieTitle = findViewById(R.id.movie_title);
        movieRunTime = findViewById(R.id.text_run_time);
        theaterName = findViewById(R.id.theater_name);
        screeningShowtime = findViewById(R.id.showtime);
        selectedSeat = findViewById(R.id.selected_seats);
        gridSeats = findViewById(R.id.grid_seats);
        progress = findViewById(R.id.progress);

        Picasso.with(this)
                .load(screening.getImageUrl())
                .error(R.mipmap.ic_launcher)
                .into(poster);

        movieTitle.setText(screening.getTitle());

        int t = screening.getRunningTime();
        int hours = t / 60; //since both are ints, you get an int
        int minutes = t % 60;

        if (screening.getRunningTime() == 0) {
            movieRunTime.setVisibility(View.GONE);
        } else if (hours > 1) {
            String translatedRunTime = hours + " hours " + minutes + " minutes";
            movieRunTime.setText(translatedRunTime);
        } else {
            String translatedRunTime = hours + " hour " + minutes + " minutes";
            movieRunTime.setText(translatedRunTime);
        }

        theaterName.setText(screening.getTheaterName());
        screeningShowtime.setText(showtime);

        //PerformanceInfo
        int normalizedMovieId = screening.getMoviepassId();
        Log.d("showtime", showtime);
        String externalMovieId = screening.getProvider().getPerformanceInfo(showtime).getExternalMovieId();
        String format = screening.getFormat();
        int tribuneTheaterId = screening.getTribuneTheaterId();
        int performanceNumber = screening.getProvider().getPerformanceInfo(showtime).getPerformanceNumber();
        String sku = screening.getProvider().getPerformanceInfo(showtime).getSku();
        Double price = screening.getProvider().getPerformanceInfo(showtime).getPrice();
        String dateTime = screening.getProvider().getPerformanceInfo(showtime).getDateTime();
        String auditorium = screening.getProvider().getPerformanceInfo(showtime).getAuditorium();
        String performanceId = screening.getProvider().getPerformanceInfo(showtime).getPerformanceId();
        String sessionId = screening.getProvider().getPerformanceInfo(showtime).getSessionId();
        int theater = screening.getProvider().getTheater();

        PerformanceInfoRequest performanceInfoRequest =  new PerformanceInfoRequest(dateTime, externalMovieId, performanceNumber,
                tribuneTheaterId, format, normalizedMovieId, sku, price, auditorium, performanceId, sessionId);

        getSeats(tribuneTheaterId, theater, performanceInfoRequest);
    }

    protected void getSeats(int tribuneTheaterId, int theater, PerformanceInfoRequest performanceInfoRequest) {
        progress.setVisibility(View.VISIBLE);

        RestClient.getAuthenticated().getSeats(tribuneTheaterId,
                String.valueOf(theater), performanceInfoRequest).enqueue(
                new Callback<SeatingsInfoResponse>() {
                    @Override
                    public void onResponse(Call<SeatingsInfoResponse> call, Response<SeatingsInfoResponse> response) {
                        progress.setVisibility(View.GONE);

                        SeatingsInfoResponse seatingsInfoResponse = response.body();
                        if (seatingsInfoResponse != null) {
                            showSeats(seatingsInfoResponse.seatingInfo.seats, seatingsInfoResponse.seatingInfo.rows, seatingsInfoResponse.seatingInfo.columns);
                        }

                    }

                    @Override
                    public void onFailure(Call<SeatingsInfoResponse> call, Throwable t) {
                        progress.setVisibility(View.GONE);
                        Log.d("error", "Unable to download seat information: " + t.getMessage().toString());
                    }
                });
    }

    private void showSeats(List<SeatInfo> seats, int rows, int columns) {
        gridSeats.setColumnCount(columns);
        gridSeats.setRowCount(rows);
        gridSeats.setOrientation(GridLayout.HORIZONTAL);

        seatButtons = new ArrayList<>();

        Collections.sort(seats);
        for (SeatInfo seat : seats) {
            SeatButton seatButton = new SeatButton(this, seat);

            final int seatRow = seat.getRow();
            final int seatCol = seat.getColumn();
            final String finalSeatName = seat.getSeatName();

            seatButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View sender) {
                    Log.d("seat", "seat: " + seatRow + " col:" + seatCol);

                    if (finalSeatName != null) {
                        selectedSeat.setText(finalSeatName);
                    } else {
                        String formattedSeatName = "Row: " + seatCol + " Seat: " + seatRow;
                        selectedSeat.setText(formattedSeatName);
                    }

                    SeatButton button = (SeatButton) sender;

                    selectSeat(button.getSeatName());

//                    mToken.setSeatName(button.getSeatName());
                }
            });

            seatButton.setPadding(3, 3, 3, 3);
            gridSeats.addView(seatButton);
            seatButtons.add(seatButton);
        }
    }

    private void selectSeat(String seatName) {
        for (SeatButton button : seatButtons) {
            button.setSeatSelected(button.getSeatName().matches(seatName));
        }
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getParcelableExtra(THEATER) != null) {
            Theater theater = Parcels.unwrap(getIntent().getParcelableExtra(THEATER));

            Intent intent = new Intent(SelectSeatActivity.this, TheaterActivity.class);
            intent.putExtra(TheaterActivity.THEATER, Parcels.wrap(theater));
            startActivity(intent);
        } else if (getIntent().getParcelableExtra(MOVIE) != null){
            Movie movie = Parcels.unwrap(getIntent().getParcelableExtra(MOVIE));

            Intent intent = new Intent(SelectSeatActivity.this, MovieActivity.class);
            intent.putExtra(TheaterActivity.THEATER, Parcels.wrap(movie));
            startActivity(intent);
        } else {
            super.onBackPressed();
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
                } else if (itemId == R.id.action_reservations) {
                    Toast.makeText(SelectSeatActivity.this, "E-Ticket Activity", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), ReservationsActivity.class));
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
