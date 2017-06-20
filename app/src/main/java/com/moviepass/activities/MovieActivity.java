package com.moviepass.activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.BuildConfig;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v13.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.moviepass.Constants;
import com.moviepass.MovieTheaterClickListener;
import com.moviepass.R;
import com.moviepass.ShowtimeClickListener;
import com.moviepass.UserLocationManagerFused;
import com.moviepass.adapters.MovieShowtimesAdapter;
import com.moviepass.adapters.MovieTheatersAdapter;
import com.moviepass.adapters.TheaterShowtimesAdapter;
import com.moviepass.fragments.MoviesFragment;
import com.moviepass.fragments.TheatersFragment;
import com.moviepass.helpers.BottomNavigationViewHelper;
import com.moviepass.model.Movie;
import com.moviepass.model.Screening;
import com.moviepass.model.Theater;
import com.moviepass.network.RestClient;
import com.moviepass.responses.ScreeningsResponse;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by anubis on 6/9/17.
 */

public class MovieActivity extends BaseActivity implements MovieTheaterClickListener, ShowtimeClickListener {

    public static final String MOVIE = "movie";
    private static final String TAG = "TAG";

    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    LocationUpdateBroadCast mLocationBroadCast;
    boolean mLocationAcquired;
    private Location mMyLocation;

    Movie mMovie;
    protected BottomNavigationView bottomNavigationView;
    MovieTheatersAdapter mMovieTheatersAdapter;
    MovieShowtimesAdapter mMovieShowtimesAdapter;
    ScreeningsResponse mScreeningsResponse;
    Screening mScreening;

    ImageView mPoster;
    TextView mTitle;
    TextView mGenre;
    TextView mRunTime;
    TextView mTheaterSelectTime;
    Button mAction;

    ArrayList<Screening> mScreeningsList;
    ArrayList<String> mShowtimesList;

    @BindView(R.id.recycler_view_theaters)
    RecyclerView mTheatersRecyclerView;
    @BindView(R.id.recycler_view_showtimes)
    RecyclerView mShowtimesRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        supportPostponeEnterTransition();

        Bundle extras = getIntent().getExtras();
        mMovie = Parcels.unwrap(getIntent().getParcelableExtra(MOVIE));

        mPoster = findViewById(R.id.poster);
        mTitle = findViewById(R.id.movie_title);
        mRunTime = findViewById(R.id.text_run_time);
        mTheaterSelectTime = findViewById(R.id.theater_select_time);
        mAction = findViewById(R.id.button_action);

        //Start location tasks
        UserLocationManagerFused.getLocationInstance(this).startLocationUpdates();
        mLocationBroadCast = new LocationUpdateBroadCast();
        registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));

        currentLocationTasks();

        bottomNavigationView = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String imageTransitionName = extras.getString(MoviesFragment.EXTRA_MOVIE_IMAGE_TRANSITION_NAME);
            mPoster.setTransitionName(imageTransitionName);
        }

        Picasso.with(this)
                .load(mMovie.getImageUrl())
                .error(R.mipmap.ic_launcher)
                .into(mPoster, new Callback() {
                    @Override
                    public void onSuccess() {
                        supportStartPostponedEnterTransition();
                    }

                    @Override
                    public void onError() {
                        supportStartPostponedEnterTransition();
                    }
                });


        mTitle.setText(mMovie.getTitle());
        int t = mMovie.getRunningTime();
        int hours = t / 60; //since both are ints, you get an int
        int minutes = t % 60;

        if (t == 0) {
            mRunTime.setVisibility(View.GONE);
        } else if (hours > 1) {
            String translatedRunTime = hours + " hours " + minutes + " minutes";
            mRunTime.setText(translatedRunTime);
        } else {
            String translatedRunTime = hours + " hour " + minutes + " minutes";
            mRunTime.setText(translatedRunTime);
        }

        mScreeningsList = new ArrayList<>();

        /* Theaters RecyclerView */
        LinearLayoutManager moviesLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        mTheatersRecyclerView = findViewById(R.id.recycler_view_theaters);
        mTheatersRecyclerView.setLayoutManager(moviesLayoutManager);

        mMovieTheatersAdapter = new MovieTheatersAdapter(mScreeningsList, this);

        mTheatersRecyclerView.setAdapter(mMovieTheatersAdapter);

        /* Showtimes RecyclerView */

        mShowtimesList = new ArrayList<>();

        mShowtimesRecyclerView = findViewById(R.id.recycler_view_showtimes);
        mShowtimesRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        mMovieShowtimesAdapter = new MovieShowtimesAdapter(mShowtimesList, mScreening, this);

        mShowtimesRecyclerView.setAdapter(mMovieShowtimesAdapter);

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
            UserLocationManagerFused.getLocationInstance(MovieActivity.this).stopLocationUpdates();
            onLocationChanged(UserLocationManagerFused.getLocationInstance(context).mCurrentLocation);
        }
    }

    protected void onLocationChanged(Location location) {
        UserLocationManagerFused.getLocationInstance(this).stopLocationUpdates();

        if (location != null) {
            UserLocationManagerFused.getLocationInstance(this).updateLocation(location);

            mMyLocation = location;

            loadTheaters(mMyLocation.getLatitude(), mMyLocation.getLongitude(), mMovie.getId());

            mLocationAcquired = true;
        }
    }

    public void currentLocationTasks() {
        registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));
        UserLocationManagerFused.getLocationInstance(MovieActivity.this).startLocationUpdates();
        mLocationAcquired = false;

        boolean enabled = UserLocationManagerFused.getLocationInstance(MovieActivity.this).isLocationEnabled();
        if (!enabled) {
//            showDialogGPS();
        } else {
            Location location = UserLocationManagerFused.getLocationInstance(MovieActivity.this).mCurrentLocation;
            onLocationChanged(location);

            if (location != null) {
                UserLocationManagerFused.getLocationInstance(this).requestLocationForCoords(location.getLatitude(), location.getLongitude(), MovieActivity.this);
            }
        }
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);

        try {
            unregisterReceiver(mLocationBroadCast);
        } catch (IllegalArgumentException is) {
            is.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));
        } catch (IllegalArgumentException is) {
            is.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));
        } catch (IllegalArgumentException is) {
            is.printStackTrace();
        }
    }

    public void onTheaterClick(int pos, Screening screening) {
        /* TODO : Get the screenings for that movie */

        ArrayList<String> startTimes = new ArrayList<>(screening.getStartTimes());

        String atWhatTime = getResources().getString(R.string.activity_theater_movie_time);
        mTheaterSelectTime.setText(screening.getTitle() + " " + atWhatTime);
        mTheaterSelectTime.setVisibility(View.VISIBLE);
        mShowtimesRecyclerView.setVisibility(View.VISIBLE);

        mShowtimesList.clear();

        if (mShowtimesRecyclerView != null) {
            mShowtimesRecyclerView.getRecycledViewPool().clear();
            mMovieShowtimesAdapter.notifyDataSetChanged();
        }

        mShowtimesList.addAll(startTimes);

        mShowtimesRecyclerView.requestFocus();

        /* TODO change it so it passes the screening & the showtimes separartely to retrieve for checkin */
    }

    public void onShowtimeClick(int pos, Screening screening, String string) {

        mAction.requestFocus();
        mAction.setVisibility(View.VISIBLE);
    }

    private void loadTheaters(Double latitude, Double longitude, int moviepassId) {
        RestClient.getAuthenticated().getScreeningsForMovie(latitude, longitude, moviepassId)
                .enqueue(new retrofit2.Callback<ScreeningsResponse>() {
                    @Override
                    public void onResponse(Call<ScreeningsResponse> call, final Response<ScreeningsResponse> response) {
                        ScreeningsResponse screenings = response.body();
                        if (screenings != null) {
                            List<Screening> screeningsList = screenings.getScreenings();

                            if (screeningsList.size() == 0) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MovieActivity.this, R.style.AlertDialogCustom);

/*                            builder.setTitle(R.string.activity_location_no_theaters_found);
                    builder.setMessage(R.string.activity_location_try_another_zip_code);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }); */

                                builder.show();
                            } else {

                                //Initial View to Display RecyclerView Based on User's Current Location
                                mScreeningsResponse = response.body();
                                mScreeningsList.clear();

                                if (mMovieTheatersAdapter != null) {
                                    mTheatersRecyclerView.getRecycledViewPool().clear();
                                    mMovieTheatersAdapter.notifyDataSetChanged();
                                }

                                if (mScreeningsResponse != null) {
                                    Log.d("getScreenings", mScreeningsResponse.getScreenings().toString());
                                    mScreeningsList.addAll(mScreeningsResponse.getScreenings());
                                    mTheatersRecyclerView.setAdapter(mMovieTheatersAdapter);
                                }

                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ScreeningsResponse> call, Throwable t) {
                        if (t != null) {
                            Log.d("Unable to get theaters", "Unable to download theaters: " + t.getMessage());
                        }
                    }

                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
//                        updateUI();
                        break;
                }
                break;
        }
    }

    /* PERMISSIONS */

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
/*            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MovieActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    }); */
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MovieActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
/*                if (mRequestingLocationUpdates) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates");
                } */
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
/*                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }); */
            }
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /* Bottom Navigation View */

    int getContentViewId() {
        return R.layout.activity_settings;
    }

    int getNavigationMenuItemId() {
        return R.id.action_settings;
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
                    Toast.makeText(MovieActivity.this, "E-Ticket Activity", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), ETicketsActivity.class));
                } else if (itemId == R.id.action_browse) {
                    startActivity(new Intent(getApplicationContext(), BrowseActivity.class));
                } else if (itemId == R.id.action_notifications) {
                    Toast.makeText(MovieActivity.this, "Notification Activity", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), NotificationsActivity.class));
                } else if (itemId == R.id.action_settings) {
                    Toast.makeText(MovieActivity.this, "Settings Activity", Toast.LENGTH_LONG).show();
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