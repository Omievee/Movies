package com.moviepass.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.moviepass.R;
import com.moviepass.ScreeningPosterClickListener;
import com.moviepass.ShowtimeClickListener;
import com.moviepass.adapters.TheaterMoviesAdapter;
import com.moviepass.adapters.TheaterShowtimesAdapter;
import com.moviepass.helpers.BottomNavigationViewHelper;
import com.moviepass.model.Movie;
import com.moviepass.model.Screening;
import com.moviepass.model.Theater;
import com.moviepass.network.RestClient;
import com.moviepass.responses.ScreeningsResponse;

import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 6/8/17.
 */

public class TheaterActivity extends BaseActivity implements ScreeningPosterClickListener, ShowtimeClickListener {

    public static final String THEATER = "theater";

    TheaterMoviesAdapter mTheaterMoviesAdapter;
    TheaterShowtimesAdapter mTheaterShowtimesAdapter;

    ArrayList<Screening> mMoviesList;
    ArrayList<String> mShowtimesList;

    protected BottomNavigationView bottomNavigationView;

    Theater mTheater;
    ScreeningsResponse mScreeningsResponse;
    Screening mScreening;
    TextView mTheaterName;
    TextView mTheaterAddress;

    @BindView(R.id.recycler_view_movies)
    RecyclerView mMoviesRecyclerView;
    @BindView(R.id.recycler_view_showtimes)
    RecyclerView mShowtimesRecyclerView;
    @BindView(R.id.movie_select_time)
    TextView mMovieSelectTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theater);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        Bundle extras = getIntent().getExtras();
        mTheater = Parcels.unwrap(getIntent().getParcelableExtra(THEATER));

        mTheaterName = (TextView) findViewById(R.id.theater_name);
        mTheaterAddress = (TextView) findViewById(R.id.theater_address);
        mMovieSelectTime = (TextView) findViewById(R.id.movie_select_time);

        mTheaterName.setText(mTheater.getName());
        mTheaterAddress.setText(mTheater.getAddress());

        mMoviesList = new ArrayList<>();
        mShowtimesList = new ArrayList<>();

         /* Movies RecyclerView */
        LinearLayoutManager moviesLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        mMoviesRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_movies);
        mMoviesRecyclerView.setLayoutManager(moviesLayoutManager);

        mTheaterMoviesAdapter = new TheaterMoviesAdapter(mMoviesList, this);

        mMoviesRecyclerView.setAdapter(mTheaterMoviesAdapter);

        loadMovies();

        /* Showtimes RecyclerView */

        mShowtimesRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_showtimes);
        mShowtimesRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        mTheaterShowtimesAdapter = new TheaterShowtimesAdapter(mShowtimesList, this);

        mShowtimesRecyclerView.setAdapter(mTheaterShowtimesAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    private void loadMovies() {
        int theaterId = mTheater.getTribuneTheaterId();

        RestClient.getAuthenticated().getScreeningsForTheater(theaterId).enqueue(new Callback<ScreeningsResponse>() {
            @Override
            public void onResponse(Call<ScreeningsResponse> call, Response<ScreeningsResponse> response) {
                mScreeningsResponse = response.body();

                if (mScreeningsResponse != null) {

                    mMoviesList.clear();

                    if (mMoviesRecyclerView != null) {
                        mMoviesRecyclerView.getRecycledViewPool().clear();
                        mTheaterMoviesAdapter.notifyDataSetChanged();
                    }

                    mMoviesList.addAll(mScreeningsResponse.getScreenings());

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

    public void onScreeningPosterClick(int pos, Screening screening, List<String> startTimes, ImageView sharedImageView) {
        /* TODO : Get the screenings for that movie */

        mMovieSelectTime.setVisibility(View.VISIBLE);
        mShowtimesRecyclerView.setVisibility(View.VISIBLE);

        mShowtimesList.clear();

        if (mShowtimesRecyclerView != null) {
            mShowtimesRecyclerView.getRecycledViewPool().clear();
            mTheaterShowtimesAdapter.notifyDataSetChanged();
        }

        mShowtimesList.addAll(startTimes);

        /* TODO change it so it passes the screening & the showtimes separartely to retrieve for checkin */

    }

    public void onShowtimeClick(int pos, String string) {
        Log.d("String", string);

    }


    public void reserve() {

    }



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
                    Toast.makeText(TheaterActivity.this, "E-Ticket Activity", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), ETicketsActivity.class));
                } else if (itemId == R.id.action_browse) {
                } else if (itemId == R.id.action_notifications) {
                    Toast.makeText(TheaterActivity.this, "Notification Activity", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), NotificationsActivity.class));
                } else if (itemId == R.id.action_settings) {
                    Toast.makeText(TheaterActivity.this, "Settings Activity", Toast.LENGTH_LONG).show();
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
