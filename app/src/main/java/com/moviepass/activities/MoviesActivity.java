package com.moviepass.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.adapters.MovieSearchAdapter;
import com.moviepass.fragments.MoviesFragment;
import com.moviepass.helpers.BottomNavigationViewHelper;
import com.moviepass.model.Movie;
import com.moviepass.model.MoviesResponse;
import com.moviepass.network.RestClient;

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
    ListView SearchResults;
    ArrayList<Movie> movieSearchNEWRELEASE;
    ArrayList<Movie> movieSearchTOPBOXOFFICE;
    ArrayList<Movie> movieSearchALLMOVIES;

    SimpleDraweeView featuredFilmHeader;

    MovieSearchAdapter searchAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        final Toolbar toolbar = findViewById(R.id.MovieToolbar_MAINPAGE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Fragment moviesFragment = new MoviesFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.MAIN_CONTAINER, moviesFragment).commit();

        FrameLayout main = findViewById(R.id.MAIN_CONTAINER);
        fadeIn(main);


        bottomNavigationView = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        SearchResults = findViewById(R.id.MovieSearch_Results);

        movieSearchNEWRELEASE = new ArrayList<>();
        movieSearchALLMOVIES = new ArrayList<>();
        movieSearchTOPBOXOFFICE = new ArrayList<>();



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
        // do nothing. We want to force user to stay in this activity and not drop out.
        AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(MoviesActivity.this, R.style.AlertDialogCustom);
        builder.setMessage("Do you want to quit MoviePass?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finishAffinity(); // finish activity
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
    }

    //Search For Movies
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (android.support.v7.widget.SearchView) menu.findItem(R.id.moviesearch).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchAdapter = new MovieSearchAdapter(getApplicationContext(), movieSearchALLMOVIES);

        movieSearchALLMOVIES.clear();
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                searchAdapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String s) {
                RestClient.getAuthenticated().getMovies(UserPreferences.getLatitude(), UserPreferences.getLongitude()).enqueue(new Callback<MoviesResponse>() {
                    @Override
                    public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                        if (response.body() != null && response.isSuccessful()) {
                            MoviesResponse = response.body();
                            SearchResults.setAdapter(searchAdapter);
                            movieSearchALLMOVIES.clear();

                            movieSearchALLMOVIES.addAll(MoviesResponse.getComingSoon());
                            movieSearchALLMOVIES.addAll(MoviesResponse.getNewReleases());
                            movieSearchALLMOVIES.addAll(MoviesResponse.getTopBoxOffice());
                            for (int i = 0; i < movieSearchALLMOVIES.size(); i++) {
                                if (!s.isEmpty()) {
                                    if (movieSearchALLMOVIES.get(i).getTitle().contains(s)) {
                                        SearchResults.setVisibility(View.VISIBLE);

                                    }
                                }
                            }

                        }
                    }

                    @Override
                    public void onFailure(Call<MoviesResponse> call, Throwable throwable) {
                        Toast.makeText(MoviesActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            }
        });


        return true;
    }

}
