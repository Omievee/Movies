package com.mobile.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mobile.UserPreferences;
import com.mobile.adapters.MovieSearchAdapter;
import com.mobile.adapters.SearchAdapter;
import com.mobile.fragments.MoviesFragment;
import com.mobile.fragments.PastReservations;
import com.mobile.fragments.PendingReservationFragment;
import com.mobile.helpers.BottomNavigationViewHelper;
import com.mobile.model.Movie;
import com.mobile.model.MoviesResponse;
import com.mobile.network.RestClient;
import com.moviepass.R;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 8/4/17.
 */

public class MoviesActivity extends BaseActivity {
    String TAG = "found it";
    public android.support.v7.widget.SearchView searchView;
    MoviesResponse MoviesResponse;
    MovieSearchAdapter searchAdapter;
    ListView SearchResults;
    ArrayList<Movie> movieSearchNEWRELEASE;
    ArrayList<Movie> movieSearchTOPBOXOFFICE;
    ArrayList<Movie> movieSearchALLMOVIES;
    FloatingActionMenu reservationsMenu;

    boolean firstBoot;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);


        Fragment moviesFragment = new MoviesFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.MAIN_CONTAINER, moviesFragment).commit();
        FrameLayout main = findViewById(R.id.MAIN_CONTAINER);
        fadeIn(main);
        bottomNavigationView = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);


        movieSearchNEWRELEASE = new ArrayList<>();
        movieSearchALLMOVIES = new ArrayList<>();
        movieSearchTOPBOXOFFICE = new ArrayList<>();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean firstBoot = prefs.getBoolean(getString(R.string.firstBoot), true);


        View parentLayout = findViewById(R.id.COORDPARENT);
        checkRestrictions();
        if (UserPreferences.getIsSubscriptionActivationRequired()) {
            Snackbar snack = Snackbar.make(parentLayout, "Activate your MoviePass card", Snackbar.LENGTH_INDEFINITE);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snack.getView().getLayoutParams();
            params.setMargins(0, 0, 0, 180);
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

        } else if (!UserPreferences.getIsSubscriptionActivationRequired()) {
//            if(!firstBoot)
        }

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

    int getContentViewId() {
        return R.layout.activity_movies;
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
                if (android.R.id.home == itemId) {
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
                } else {
                    if (itemId == R.id.action_profile) {
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
            }
        }, 50);
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


}
