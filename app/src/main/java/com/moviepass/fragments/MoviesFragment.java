package com.moviepass.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v13.view.ViewCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.moviepass.Constants;
import com.moviepass.MoviePosterClickListener;
import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.activities.ActivateMoviePassCard;
import com.moviepass.activities.MovieActivity;
import com.moviepass.adapters.MoviesComingSoonAdapter;
import com.moviepass.adapters.MoviesNewReleasesAdapter;
import com.moviepass.adapters.MoviesTopBoxOfficeAdapter;
import com.moviepass.model.Movie;
import com.moviepass.model.MoviesResponse;
import com.moviepass.network.Api;
import com.moviepass.network.RestClient;
import com.moviepass.requests.CardActivationRequest;
import com.moviepass.responses.CardActivationResponse;

import org.parceler.Parcels;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ryan on 4/25/17.
 */

public class MoviesFragment extends Fragment implements MoviePosterClickListener, LocationListener {

    public static final String MOVIES = "movies";
    public static final String EXTRA_MOVIE_IMAGE_TRANSITION_NAME = "movie_image_transition_name";
    public static final String EXTRA_MOVIE_ITEM = "movie_image_url";

    public static final int LOCATION_PERMISSIONS = 99;
    android.location.LocationManager LocationManager;
    String Provider;

    Api api;
    FloatingActionMenu reservationsMenu;

    private MoviesNewReleasesAdapter mMoviesNewReleasesAdapter;
    private MoviesTopBoxOfficeAdapter mMoviesTopBoxOfficeAdapter;
    private MoviesComingSoonAdapter mMoviesComingSoonAdapter;

    MoviesFragment mMoviesFragment;
    MoviesResponse moviesResponse;
    ArrayList<Movie> mMovieArrayList;

    ArrayList<Movie> TopBoxOffice;
    ArrayList<Movie> comingSoon;
    ArrayList<Movie> newReleases;

    @BindView(R.id.new_releases)
    RecyclerView mNewReleasesRecyclerView;
    @BindView(R.id.top_box_office)
    RecyclerView mTopBoxOfficeRecyclerView;
    @BindView(R.id.coming_soon)
    RecyclerView mComingSoonRecyclerView;

    @BindView(R.id.MoviePass_HEADER)
    ImageView MovieHeader;

    @BindView(R.id.MAINPAGE_FEATURED)
    SimpleDraweeView featuredFilmHeader;

//    @BindView(R.id.MOVIES_SEARCH)
//    SearchView MovieSearch;

    View progress;

    private OnFragmentInteractionListener listener;

    public static MoviesFragment newInstance() {
        return new MoviesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        ButterKnife.bind(this, rootView);

        progress = rootView.findViewById(R.id.progress);

        Api api;

        newReleases = new ArrayList<>();
        TopBoxOffice = new ArrayList<>();
        comingSoon = new ArrayList<>();

        /** New Releases RecyclerView */
        LinearLayoutManager newReleasesLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        mNewReleasesRecyclerView = rootView.findViewById(R.id.new_releases);
        mNewReleasesRecyclerView.setLayoutManager(newReleasesLayoutManager);
        mNewReleasesRecyclerView.setItemAnimator(null);
        fadeIn(mNewReleasesRecyclerView);
        reservationsMenu = rootView.findViewById(R.id.FAB_RESERVATION_MENU);
        mMoviesNewReleasesAdapter = new MoviesNewReleasesAdapter(getActivity(), newReleases, this);

        /** Top Box Office RecyclerView */
        LinearLayoutManager topBoxOfficeLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        mTopBoxOfficeRecyclerView = rootView.findViewById(R.id.top_box_office);
        mTopBoxOfficeRecyclerView.setLayoutManager(topBoxOfficeLayoutManager);
        mTopBoxOfficeRecyclerView.setItemAnimator(null);
        fadeIn(mTopBoxOfficeRecyclerView);
        mMoviesTopBoxOfficeAdapter = new MoviesTopBoxOfficeAdapter(getActivity(), TopBoxOffice, this);

        /** Coming Soon RecyclerView */
        LinearLayoutManager comingSoonLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mComingSoonRecyclerView = rootView.findViewById(R.id.coming_soon);
        mComingSoonRecyclerView.setLayoutManager(comingSoonLayoutManager);
        mComingSoonRecyclerView.setItemAnimator(null);
        fadeIn(mComingSoonRecyclerView);
        mMoviesComingSoonAdapter = new MoviesComingSoonAdapter(getActivity(), comingSoon, this);


        progress.setVisibility(View.VISIBLE);


        loadMovies();

//        if (isPendingSubscription()) {
//            showActivateCardDialog();
//        }

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        LocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Provider = LocationManager.getBestProvider(criteria, true);
//        Location location = LocationManager.getLastKnownLocation(Provider);


        checkLocationPermission();
        Log.d(Constants.TAG, "onCreateView: " + UserPreferences.getRestrictionHasActiveCard());
        Log.d(Constants.TAG, "onCreateView: " + UserPreferences.getRestrictionSubscriptionStatus());


        //Check for active moviepass card or not
        if (!UserPreferences.getRestrictionHasActiveCard()) {
            Snackbar snack = Snackbar.make(rootView, "Activate your MoviePass card", Snackbar.LENGTH_INDEFINITE);
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snack.getView().getLayoutParams();
            snack.getView().setLayoutParams(params);
            snack.show();
            View sb = snack.getView();
            snack.getView().setHovered(true);
            sb.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            sb.setBackgroundColor(getResources().getColor(R.color.new_red));
            snack.setActionTextColor(getResources().getColor(R.color.white));
            snack.setAction("Ok", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent activateCard = new Intent(getActivity(), ActivateMoviePassCard.class);
                    startActivity(activateCard);
                }
            });
            reservationsMenu.setVisibility(View.GONE);
        }

        final FloatingActionButton currentRes = new FloatingActionButton(getActivity());
        currentRes.setLabelText("Current Reservation");
        currentRes.setButtonSize(FloatingActionButton.SIZE_MINI);
        FloatingActionButton historyRes = new FloatingActionButton(getActivity());
        historyRes.setLabelText("Past Reservations");
        historyRes.setButtonSize(FloatingActionButton.SIZE_MINI);
        historyRes.setShowProgressBackground(true);

        reservationsMenu.addMenuButton(currentRes);
        reservationsMenu.addMenuButton(historyRes);

        currentRes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PendingReservationFragment fragobj = new PendingReservationFragment();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fragobj.show(fm, "fragment_pendingreservation");
                reservationsMenu.close(true);

            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mMoviesNewReleasesAdapter != null) {
            mNewReleasesRecyclerView.setAdapter(mMoviesNewReleasesAdapter);
        }

        if (mTopBoxOfficeRecyclerView != null) {
            mTopBoxOfficeRecyclerView.setAdapter(mMoviesTopBoxOfficeAdapter);
        }

        if (mComingSoonRecyclerView != null) {
            mComingSoonRecyclerView.setAdapter(mMoviesComingSoonAdapter);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Request location updates:
                Toast.makeText(getActivity(), "GPS Location Is Required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        /*if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    //TODO:
    public void onMoviePosterClick(int pos, Movie movie, ImageView sharedImageView) {
        Intent movieIntent = new Intent(getActivity(), MovieActivity.class);
        movieIntent.putExtra(MovieActivity.MOVIE, Parcels.wrap(movie));
        movieIntent.putExtra(EXTRA_MOVIE_IMAGE_TRANSITION_NAME, ViewCompat.getTransitionName(sharedImageView));
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(),
                sharedImageView,
                ViewCompat.getTransitionName(sharedImageView));
        startActivity(movieIntent, options.toBundle());
    }

    public void loadMovies() {
        RestClient.getAuthenticated().getMovies(UserPreferences.getLatitude(), UserPreferences.getLongitude()).enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {

                if (response.body() != null && response.isSuccessful()) {
                    progress.setVisibility(View.GONE);
                    moviesResponse = response.body();

                    Log.d(Constants.TAG, "onResponse: " + moviesResponse.getFeatured());

                    newReleases.clear();
                    TopBoxOffice.clear();
                    comingSoon.clear();

                    if (mMoviesNewReleasesAdapter != null) {
                        mNewReleasesRecyclerView.getRecycledViewPool().clear();
                        mMoviesNewReleasesAdapter.notifyDataSetChanged();
                    }

                    if (mMoviesTopBoxOfficeAdapter != null) {
                        mTopBoxOfficeRecyclerView.getRecycledViewPool().clear();
                        mMoviesTopBoxOfficeAdapter.notifyDataSetChanged();
                    }

                    if (mMoviesComingSoonAdapter != null) {
                        mComingSoonRecyclerView.getRecycledViewPool().clear();
                        mMoviesComingSoonAdapter.notifyDataSetChanged();
                    }

                    if (moviesResponse != null) {
                        newReleases.addAll(moviesResponse.getNewReleases());
                        mNewReleasesRecyclerView.setAdapter(mMoviesNewReleasesAdapter);

                        TopBoxOffice.addAll(moviesResponse.getTopBoxOffice());
                        mTopBoxOfficeRecyclerView.setAdapter(mMoviesTopBoxOfficeAdapter);

                        comingSoon.addAll(moviesResponse.getComingSoon());
                        mComingSoonRecyclerView.setAdapter(mMoviesComingSoonAdapter);

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

    public boolean isPendingSubscription() {
        if (UserPreferences.getRestrictionSubscriptionStatus().matches("PENDING_ACTIVATION") ||
                UserPreferences.getRestrictionSubscriptionStatus().matches("PENDING_FREE_TRIAL")) {
            return true;
        } else {
            return false;
        }
    }

    private void showActivateCardDialog() {
        View dialoglayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_activate_card, null);
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(getActivity());
        alert.setView(dialoglayout);

        final EditText editText = dialoglayout.findViewById(R.id.activate_card);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(4);
        editText.setFilters(filters);

        alert.setTitle(getString(R.string.dialog_activate_card_header));
        alert.setMessage(R.string.dialog_activate_card_enter_card_digits);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                String digits = editText.getText().toString();
                dialog.dismiss();

                if (digits.length() == 4) {
                    CardActivationRequest request = new CardActivationRequest(digits);
                    progress.setVisibility(View.VISIBLE);

                    RestClient.getAuthenticated().activateCard(request).enqueue(new retrofit2.Callback<CardActivationResponse>() {
                        @Override
                        public void onResponse(Call<CardActivationResponse> call, Response<CardActivationResponse> response) {
                            CardActivationResponse cardActivationResponse = response.body();
                            progress.setVisibility(View.GONE);

                            if (cardActivationResponse != null && response.isSuccessful()) {
                                String cardActivationResponseMessage = cardActivationResponse.getMessage();
                                Toast.makeText(getActivity(), R.string.dialog_activate_card_successful, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getActivity(), R.string.dialog_activate_card_bad_four_digits, Toast.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        public void onFailure(Call<CardActivationResponse> call, Throwable t) {
                            progress.setVisibility(View.GONE);
                            showActivateCardDialog();
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), R.string.dialog_activate_card_must_enter_four_digits, Toast.LENGTH_LONG).show();
                }
            }
        });
        alert.setNegativeButton("Activate Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), R.string.dialog_activate_card_must_activate_future, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        alert.show();
    }

    public interface OnFragmentInteractionListener {
    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle("GPS Services Are Required For MoviePass to Run Properlly")
                        .setMessage(" Allow GPS Location Access? ")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_PERMISSIONS);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSIONS);
            }
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        LocationManager.requestLocationUpdates(Provider, 400, 1, this);
                    }

                } else {
                    Toast.makeText(getActivity(), "GPS Permissions Are Required. Go To App Settings.", Toast.LENGTH_SHORT).show();

                }
            }

        }
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public void fadeIn(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(1000);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        view.setAnimation(animation);

    }

    public void fadeOut(View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new DecelerateInterpolator()); //add this
        fadeOut.setDuration(1000);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeOut);
        view.setAnimation(animation);
    }


}





