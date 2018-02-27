package com.mobile.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mobile.Constants;
import com.mobile.MoviePosterClickListener;
import com.mobile.UserPreferences;
import com.mobile.activities.MovieActivity;
import com.mobile.adapters.FeaturedAdapter;
import com.mobile.adapters.MoviesComingSoonAdapter;
import com.mobile.adapters.MoviesNewReleasesAdapter;
import com.mobile.adapters.MoviesTopBoxOfficeAdapter;
import com.mobile.adapters.NowPlayingMoviesAdapter;
import com.mobile.adapters.SearchAdapter;
import com.mobile.helpers.ContextSingleton;
import com.mobile.model.Movie;
import com.mobile.model.MoviesResponse;
import com.mobile.network.Api;
import com.mobile.network.RestClient;
import com.mobile.requests.CardActivationRequest;
import com.mobile.responses.CardActivationResponse;
import com.moviepass.R;

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
    TextView newReleaseTXT, nowPlayingTXT, comingSoonTXT, topBoxTXT;
    Api api;

    private MoviesNewReleasesAdapter newRealeasesAdapter;
    private MoviesTopBoxOfficeAdapter topBoxOfficeAdapter;
    private MoviesComingSoonAdapter comingSoonAdapter;
    private NowPlayingMoviesAdapter nowPlayingAdapter;
    private FeaturedAdapter featuredAdapter;
    SearchAdapter customAdapter;
    MaterialSearchBar searchBar;
    MoviesResponse moviesResponse;
    SearchFragment fragment = new SearchFragment();
    ImageView movieLogo, searchicon;
    ArrayList<Movie> TopBoxOffice;
    ArrayList<Movie> comingSoon;
    ArrayList<Movie> newReleases;
    ArrayList<Movie> featured;
    ArrayList<Movie> nowPlaying;
    public ArrayList<Movie> ALLMOVIES;
    ArrayList<String> lastSuggestions;
    Activity myActivity;

    @BindView(R.id.new_releases)
    RecyclerView newReleasesRecycler;
    @BindView(R.id.top_box_office)
    RecyclerView topBoxOfficeRecycler;
    @BindView(R.id.coming_soon)
    RecyclerView comingSoonRecycler;
    @BindView(R.id.now_playing)
    RecyclerView nowPlayingRecycler;

    @BindView(R.id.FeaturedRE)
    RecyclerView featuredRecycler;


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
        newReleaseTXT = rootView.findViewById(R.id.new_releases_text);
        newReleaseTXT.setVisibility(View.GONE);
        nowPlayingTXT = rootView.findViewById(R.id.now_Playing_text);
        nowPlayingTXT.setVisibility(View.GONE);
        comingSoonTXT = rootView.findViewById(R.id.coming_soon_text);
        comingSoonTXT.setVisibility(View.GONE);
        topBoxTXT = rootView.findViewById(R.id.top_box_office_text);
        topBoxTXT.setVisibility(View.GONE);
        searchicon = rootView.findViewById(R.id.search_inactive);
        searchicon.setVisibility(View.GONE);
        movieLogo = rootView.findViewById(R.id.MoviePass_HEADER);
        Api api;

        newReleases = new ArrayList<>();
        TopBoxOffice = new ArrayList<>();
        comingSoon = new ArrayList<>();
        featured = new ArrayList<>();
        nowPlaying = new ArrayList<>();
        ALLMOVIES = new ArrayList<>();

        int resId = R.anim.layout_animation;
        int res2 = R.anim.layout_anim_bottom;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), resId);
        LayoutAnimationController animation2 = AnimationUtils.loadLayoutAnimation(getContext(), res2);

        /** New Releases RecyclerView */
        LinearLayoutManager newReleasesLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        newReleasesRecycler = rootView.findViewById(R.id.new_releases);
        newReleasesRecycler.setLayoutManager(newReleasesLayoutManager);
        newReleasesRecycler.setItemAnimator(null);
        fadeIn(newReleasesRecycler);
        newReleasesRecycler.setLayoutAnimation(animation);

        newRealeasesAdapter = new MoviesNewReleasesAdapter(getActivity(), newReleases, this);

        /** Top Box Office RecyclerView */
        LinearLayoutManager topBoxOfficeLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        topBoxOfficeRecycler = rootView.findViewById(R.id.top_box_office);
        topBoxOfficeRecycler.setLayoutManager(topBoxOfficeLayoutManager);
        topBoxOfficeRecycler.setItemAnimator(null);
        fadeIn(topBoxOfficeRecycler);
        topBoxOfficeRecycler.setLayoutAnimation(animation);

        topBoxOfficeAdapter = new MoviesTopBoxOfficeAdapter(getActivity(), TopBoxOffice, this);

        /** Coming Soon RecyclerView */
        LinearLayoutManager comingSoonLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        comingSoonRecycler = rootView.findViewById(R.id.coming_soon);
        comingSoonRecycler.setLayoutManager(comingSoonLayoutManager);
        comingSoonRecycler.setItemAnimator(null);
        fadeIn(comingSoonRecycler);
        comingSoonRecycler.setLayoutAnimation(animation);

        comingSoonAdapter = new MoviesComingSoonAdapter(getActivity(), comingSoon, this);

        /** NOW PLAYING */
        LinearLayoutManager nowplayingManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        nowPlayingRecycler = rootView.findViewById(R.id.now_playing);
        nowPlayingRecycler.setLayoutManager(nowplayingManager);
        fadeIn(nowPlayingRecycler);
        nowPlayingRecycler.setLayoutAnimation(animation);

        nowPlayingAdapter = new NowPlayingMoviesAdapter(getActivity(), nowPlaying, this);

        /** FEATURED */
        LinearLayoutManager featuredManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        featuredRecycler = rootView.findViewById(R.id.FeaturedRE);
        featuredRecycler.setLayoutManager(featuredManager);
        fadeIn(featuredRecycler);
        featuredAdapter = new FeaturedAdapter(getActivity(), featured, this);
        featuredRecycler.setLayoutAnimation(animation2);

        progress.setVisibility(View.VISIBLE);
        loadMovies();

        /** SEARCH */
        searchicon.setOnClickListener(view -> {
            FragmentManager fragmentManager = getActivity().getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
            transaction.replace(R.id.MAIN_CONTAINER, fragment);
            transaction.addToBackStack(null);
            fragmentManager.popBackStack();
            transaction.commit();
        });

        //Featured Film:

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        LocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Provider = LocationManager.getBestProvider(criteria, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }


        Log.d(Constants.TAG, "onCreateView:  " + UserPreferences.getIsVerificationRequired());

        ContextSingleton.getInstance(getContext()).getGlobalContext();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (newRealeasesAdapter != null) {
            newReleasesRecycler.setAdapter(newRealeasesAdapter);
        }

        if (topBoxOfficeRecycler != null) {
            topBoxOfficeRecycler.setAdapter(topBoxOfficeAdapter);
        }

        if (comingSoonRecycler != null) {
            comingSoonRecycler.setAdapter(comingSoonAdapter);
        }
        if (nowPlayingRecycler != null) {
            Log.d(Constants.TAG, "onViewCreated: ");
            nowPlayingRecycler.setAdapter(nowPlayingAdapter);
        }

        if (featuredRecycler != null) {
            featuredRecycler.setAdapter(featuredAdapter);
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
        startActivity(movieIntent);
    }

    public void loadMovies() {
        RestClient.getAuthenticated().getMovies(UserPreferences.getLatitude(), UserPreferences.getLongitude()).enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {

                if (response.body() != null && response.isSuccessful()) {
                    progress.setVisibility(View.GONE);
                    moviesResponse = response.body();

                    Log.d(Constants.TAG, "featured: " + moviesResponse.getFeatured().toString());
                    Log.d(Constants.TAG, "now playing:  " + moviesResponse.getNowPlaying().toString());
                    Log.d(Constants.TAG, "coming soon: " + moviesResponse.getComingSoon().toString());
                    Log.d(Constants.TAG, "new relase: " + moviesResponse.getNewReleases().toString());
                    Log.d(Constants.TAG, "top box: " + moviesResponse.getTopBoxOffice().toString());

                    newReleases.clear();
                    TopBoxOffice.clear();
                    comingSoon.clear();
                    featured.clear();
                    nowPlaying.clear();
                    ALLMOVIES.clear();
                    topBoxTXT.setVisibility(View.VISIBLE);
                    fadeIn(topBoxTXT);
                    comingSoonTXT.setVisibility(View.VISIBLE);
                    fadeIn(comingSoonTXT);
                    newReleaseTXT.setVisibility(View.VISIBLE);
                    fadeIn(newReleaseTXT);
                    nowPlayingTXT.setVisibility(View.VISIBLE);
                    fadeIn(nowPlayingTXT);


                    if (newRealeasesAdapter != null) {
                        newReleasesRecycler.getRecycledViewPool().clear();
                        newRealeasesAdapter.notifyDataSetChanged();
                    }

                    if (topBoxOfficeAdapter != null) {
                        topBoxOfficeRecycler.getRecycledViewPool().clear();
                        topBoxOfficeAdapter.notifyDataSetChanged();
                    }

                    if (comingSoonAdapter != null) {
                        comingSoonRecycler.getRecycledViewPool().clear();
                        comingSoonAdapter.notifyDataSetChanged();
                    }

                    if (nowPlayingAdapter != null) {
                        nowPlayingRecycler.getRecycledViewPool().clear();
                        nowPlayingAdapter.notifyDataSetChanged();
                    }

                    if (customAdapter != null) {
                        customAdapter.notifyDataSetChanged();
                    }

                    if (moviesResponse != null) {
                        newReleases.addAll(moviesResponse.getNewReleases());
                        newReleasesRecycler.setAdapter(newRealeasesAdapter);

                        TopBoxOffice.addAll(moviesResponse.getTopBoxOffice());
                        topBoxOfficeRecycler.setAdapter(topBoxOfficeAdapter);

//                        Collections.sort(comingSoon, new Comparator<Movie>() {
//                            @Override
//                            public int compare(Movie movie, Movie t1) {
//                                final SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd");
//                                Date date = null;
//                                Date date1 = null;
//                                try {
//                                    date = fm.parse(movie.getReleaseDate());
//                                    date1 = fm.parse(t1.getReleaseDate());
//
//                                    SimpleDateFormat out = new SimpleDateFormat("MM/dd/yyyy");
////                                    holder.comingSoon.setText(out.format(date));
//
//                                } catch (ParseException e) {
//                                    e.printStackTrace();
//                                }
//
//
//                                return date.compareTo(date1);
//                            }
//                        });
                        comingSoon.addAll(moviesResponse.getComingSoon());
                        comingSoonRecycler.setAdapter(comingSoonAdapter);

                        nowPlaying.addAll(moviesResponse.getNowPlaying());
                        nowPlayingRecycler.setAdapter(nowPlayingAdapter);

                        featured.addAll(moviesResponse.getFeatured());
                        featuredRecycler.setAdapter(featuredAdapter);
                        searchicon.setVisibility(View.VISIBLE);
                        fadeIn(searchicon);
                        //Filter out duplicates
                        Log.d(Constants.TAG, "size second: " + ALLMOVIES.size());

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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myActivity = activity;

    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(myActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(myActivity)
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
                ActivityCompat.requestPermissions(myActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSIONS);
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
                    if (ContextCompat.checkSelfPermission(myActivity,
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





