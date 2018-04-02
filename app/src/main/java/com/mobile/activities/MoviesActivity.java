package com.mobile.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;

import com.helpshift.support.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;

import com.github.clans.fab.FloatingActionMenu;
import com.mobile.Constants;

import com.mobile.UserPreferences;
import com.mobile.fragments.MoviesFragment;
import com.mobile.fragments.TicketVerificationDialog;
import com.mobile.helpers.BottomNavigationViewHelper;
import com.mobile.helpers.GoWatchItSingleton;
import com.mobile.model.Eid;
import com.mobile.model.Movie;
import com.mobile.model.MoviesResponse;
import com.mobile.network.Api;
import com.mobile.network.RestCallback;
import com.mobile.network.RestClient;
import com.mobile.network.RestError;
import com.mobile.requests.OpenAppEventRequest;
import com.mobile.responses.ChangedMindResponse;
import com.mobile.responses.GoWatchItResponse;
import com.mobile.responses.RestrictionsResponse;
import com.moviepass.BuildConfig;
import com.moviepass.R;

import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.blurry.Blurry;
import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Header;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 8/4/17.
 */

public class MoviesActivity extends BaseActivity {
    ArrayList<Movie> movieSearchNEWRELEASE;
    ArrayList<Movie> movieSearchTOPBOXOFFICE;
    ArrayList<Movie> movieSearchALLMOVIES;

    //Retrofit calls
    Call<RestrictionsResponse> restrictionsResponseCall;

    public static final String MOVIES = "movies";
    View parentLayout;
    boolean firstBoot;
    public ArrayList<Movie> ALLMOVIES;
    Movie movie;
    int movieId;
    List<String> urlPath;
    String url;
    MoviesResponse moviesResponse;
    private Call<MoviesResponse> loadMoviesCall;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        Intent intent = getIntent();
        if (intent != null && intent.getIntExtra(MOVIES, -1) != -1) {
            movieId = intent.getIntExtra(MOVIES, -1);
            loadMovies();
        } else {
            Fragment moviesFragment = new MoviesFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MAIN_CONTAINER, moviesFragment).commit();
            FrameLayout main = findViewById(R.id.MAIN_CONTAINER);
            fadeIn(main);
        }
        bottomNavigationView = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);


        movieSearchNEWRELEASE = new ArrayList<>();
        movieSearchALLMOVIES = new ArrayList<>();
        movieSearchTOPBOXOFFICE = new ArrayList<>();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean firstBoot = prefs.getBoolean(getString(R.string.firstBoot), true);


        checkRestrictions();

        Log.d(Constants.TAG, "onCreate: " + UserPreferences.getRestrictionSubscriptionStatus());
        if (UserPreferences.getIsSubscriptionActivationRequired()) {
            activateMoviePassCardSnackBar();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavigationBarState();
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
        if (restrictionsResponseCall != null && !restrictionsResponseCall.isExecuted())
            restrictionsResponseCall.cancel();
        if (loadMoviesCall != null && !loadMoviesCall.isExecuted())
            loadMoviesCall.cancel();
    }

    int getContentViewId() {
        return R.layout.activity_movies;
    }

    int getNavigationMenuItemId() {
        return R.id.action_movies;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        bottomNavigationView.postDelayed(() -> {
            int itemId = item.getItemId();
            if (android.R.id.home == itemId) {
                AlertDialog alert;
                AlertDialog.Builder builder = new AlertDialog.Builder(MoviesActivity.this, R.style.AlertDialogCustom);
                builder.setMessage("Do you want to quit MoviePass?");
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    finish(); // finish activity
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.show();
                alert = builder.create();
                alert.show();
            } else {
                if (itemId == R.id.action_profile) {
                    if (loadMoviesCall != null)
                        loadMoviesCall.cancel();
                    if (restrictionsResponseCall != null)
                        restrictionsResponseCall.cancel();
                    // item.setIcon(getDrawable(R.drawable.profilenavred));
                    if (UserPreferences.getUserId() == 0) {
                        Intent intent = new Intent(MoviesActivity.this, LogInActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(MoviesActivity.this, ProfileActivity.class);
                        startActivity(intent);
                    }
                } else if (itemId == R.id.action_movies) {
                } else if (itemId == R.id.action_theaters) {
                    startActivity(new Intent(MoviesActivity.this, TheatersActivity.class));
                } else if (itemId == R.id.action_settings) {
                    startActivity(new Intent(MoviesActivity.this, SettingsActivity.class));
                }
            }
        }, 0);
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

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        if (android.R.id.home == id) {
            AlertDialog alert;
            AlertDialog.Builder builder = new AlertDialog.Builder(MoviesActivity.this, R.style.AlertDialogCustom);
            builder.setMessage("Do you want to quit MoviePass?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish(); // finish activity
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
            alert = builder.create();
            alert.show();
            return true; // true = handled manually (consumed)
        } else {
            // Default behaviour for other items
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            AlertDialog alert;
            AlertDialog.Builder builder = new AlertDialog.Builder(MoviesActivity.this, R.style.AlertDialogCustom);
            builder.setMessage("Do you want to quit MoviePass?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finishAffinity();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert = builder.create();
            alert.show();
        } else if (getFragmentManager().getBackStackEntryCount() == 1) {
            getFragmentManager().popBackStack();
        }

        // do nothing. We want to force user to stay in this activity and not drop out.

    }

    public void activateMoviePassCardSnackBar() {
        parentLayout = findViewById(R.id.COORDPARENT);
        Snackbar snack = Snackbar.make(parentLayout, "Activate your MoviePass card", Snackbar.LENGTH_INDEFINITE);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snack.getView().getLayoutParams();
        params.setMargins(0, 0, 0, 0);
        snack.getView().setLayoutParams(params);
        snack.show();
        View sb = snack.getView();
        snack.getView().setHovered(true);
        sb.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        sb.setBackgroundColor(getResources().getColor(R.color.new_red));
        snack.setActionTextColor(getResources().getColor(R.color.white));
        snack.setAction("Ok", v -> {
            Intent activateCard = new Intent(MoviesActivity.this, ActivateMoviePassCard.class);
            startActivity(activateCard);
        });
    }


    public void checkRestrictions() {
        restrictionsResponseCall = RestClient.getAuthenticated().getRestrictions(UserPreferences.getUserId() + offset);
        restrictionsResponseCall.enqueue(new Callback<RestrictionsResponse>() {
            @Override
            public void onResponse(Call<RestrictionsResponse> call, Response<RestrictionsResponse> response) {
                if (response.body() != null && response.isSuccessful()) {
                    restriction = response.body();
                    String status = restriction.getSubscriptionStatus();
                    boolean fbPresent = restriction.getFacebookPresent();
                    boolean threeDEnabled = restriction.get3dEnabled();
                    boolean allFormatsEnabled = restriction.getAllFormatsEnabled();
                    boolean proofOfPurchaseRequired = restriction.getProofOfPurchaseRequired();
                    boolean hasActiveCard = restriction.getHasActiveCard();
                    boolean subscriptionActivationRequired = restriction.isSubscriptionActivationRequired();

                    if (!UserPreferences.getRestrictionSubscriptionStatus().equals(status) ||
                            UserPreferences.getRestrictionFacebookPresent() != fbPresent ||
                            UserPreferences.getRestrictionThreeDEnabled() != threeDEnabled ||
                            UserPreferences.getRestrictionAllFormatsEnabled() != allFormatsEnabled ||
                            UserPreferences.getProofOfPurchaseRequired() != proofOfPurchaseRequired ||
                            UserPreferences.getRestrictionHasActiveCard() != hasActiveCard ||
                            UserPreferences.getIsSubscriptionActivationRequired() != subscriptionActivationRequired) {

                        UserPreferences.setRestrictions(status, fbPresent, threeDEnabled, allFormatsEnabled, proofOfPurchaseRequired, hasActiveCard, subscriptionActivationRequired);
                    }
                    //IF popInfo NOT NULL THEN INFLATE TicketVerificationActivity
                    if (UserPreferences.getProofOfPurchaseRequired() && restriction.getPopInfo() != null) {
                        int reservationId = restriction.getPopInfo().getReservationId();
                        String movieTitle = restriction.getPopInfo().getMovieTitle();
                        String tribuneMovieId = restriction.getPopInfo().getTribuneMovieId();
                        String theaterName = restriction.getPopInfo().getTheaterName();
                        String tribuneTheaterId = restriction.getPopInfo().getTribuneTheaterId();
                        String showtime = restriction.getPopInfo().getShowtime();

                        bundle = new Bundle();
                        bundle.putInt("reservationId", reservationId);
                        bundle.putString("mSelectedMovieTitle", movieTitle);
                        bundle.putString("tribuneMovieId", tribuneMovieId);
                        bundle.putString("mTheaterSelected", theaterName);
                        bundle.putString("tribuneTheaterId", tribuneTheaterId);
                        bundle.putString("showtime", showtime);

                        TicketVerificationDialog dialog = new TicketVerificationDialog();
                        FragmentManager fm = getSupportFragmentManager();
                        addFragmentOnlyOnce(fm, dialog, "fr_ticketverification_banner");
                    }
                    //Alert data to create Alert Activity on launch...
                    if (restriction.getAlert() != null) {

                        Intent activateAlert = new Intent(MoviesActivity.this, AlertActivity.class);

                        activateAlert.putExtra("title", restriction.getAlert().getTitle());
                        activateAlert.putExtra("body", restriction.getAlert().getBody());
                        activateAlert.putExtra("url", restriction.getAlert().getUrl());
                        activateAlert.putExtra("urlTitle", restriction.getAlert().getUrlTitle());
                        activateAlert.putExtra("dismissable", restriction.getAlert().isDismissable());

                        startActivity(activateAlert);


                    }

                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());

                        //IF API ERROR LOG OUT TO LOG BACK IN
                        /*
                        if (jObjError.getString("message").matches("INVALID API REQUEST")) {

                        */

                    } catch (Exception e) {

                    }
                }
            }

            public void onFailure(Call<RestrictionsResponse> call, Throwable t) {

            }
        });
    }


    public void addFragmentOnlyOnce(FragmentManager fragmentManager, TicketVerificationDialog fragment, String tag) {
        // Make sure the current transaction finishes first
        fragmentManager.executePendingTransactions();
        // If there is no fragment yet with this tag...
        if (fragmentManager.findFragmentByTag(tag) == null) {
            TicketVerificationDialog dialog = new TicketVerificationDialog();
            dialog.setArguments(bundle);
            FragmentManager fm = getSupportFragmentManager();
            dialog.setCancelable(false);
            dialog.show(fm, "fr_ticketverification_banner");
        }
    }


    public void loadMovies() {
        loadMoviesCall = RestClient.getAuthenticated().getMovies(UserPreferences.getLatitude(), UserPreferences.getLongitude());
        loadMoviesCall.enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {

                if (response.body() != null && response.isSuccessful()) {
                    moviesResponse = response.body();
                    ALLMOVIES = new ArrayList<>();

                    if (moviesResponse != null) {
                        ALLMOVIES.addAll(moviesResponse.getNewReleases());
                        ALLMOVIES.addAll(moviesResponse.getTopBoxOffice());
                        ALLMOVIES.addAll(moviesResponse.getComingSoon());
                        ALLMOVIES.addAll(moviesResponse.getNowPlaying());
                        ALLMOVIES.addAll(moviesResponse.getFeatured());

                        for (Movie AllMovies : ALLMOVIES) {
                            if (AllMovies.getId() == movieId) {
                                movie = AllMovies;
                                startMovieActivity();
                            }
                        }


                    }
                } else {
                    /* TODO : FIX IF RESPONSE IS NULL */
                }
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {

            }
        });
    }

    public void startMovieActivity() {
        Intent movieIntent = new Intent(this, MovieActivity.class);
        movieIntent.putExtra(MovieActivity.MOVIE, Parcels.wrap(movie));
        movieIntent.putExtra(MovieActivity.DEEPLINK, url);
        startActivity(movieIntent);
    }


}
