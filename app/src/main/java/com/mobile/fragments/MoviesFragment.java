package com.mobile.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Animatable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.renderscript.RenderScript;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.mobile.Constants;
import com.mobile.MoviePosterClickListener;
import com.mobile.UserPreferences;
import com.mobile.activities.MovieActivity;
import com.mobile.adapters.FeaturedAdapter;
import com.mobile.adapters.MoviesComingSoonAdapter;
import com.mobile.adapters.MoviesNewReleasesAdapter;
import com.mobile.adapters.MoviesTopBoxOfficeAdapter;
import com.mobile.adapters.NowPlayingMoviesAdapter;
import com.mobile.extensions.CustomLinearLayoutManager;
import com.mobile.extensions.LockableScrollView;
import com.mobile.helpers.LogUtils;
import com.mobile.helpers.RSBlurProcessor;
import com.mobile.model.Movie;
import com.mobile.model.MoviesResponse;
import com.mobile.network.Api;
import com.mobile.network.RestClient;
import com.mobile.responses.AllMoviesResponse;
import com.mobile.responses.HistoryResponse;
import com.mobile.responses.LocalStorageMovies;
import com.moviepass.R;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
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
    Realm historyRealm;
    RealmConfiguration historyConfig;
    public static final int LOCATION_PERMISSIONS = 99;
    android.location.LocationManager LocationManager;
    String Provider;
    TextView newReleaseTXT, nowPlayingTXT, comingSoonTXT, topBoxTXT;

    public static SwipeRefreshLayout swiper;
    public static Realm moviesRealm;
    Realm allMoviesRealm;
    private MoviesNewReleasesAdapter newRealeasesAdapter;
    private MoviesTopBoxOfficeAdapter topBoxOfficeAdapter;
    private MoviesComingSoonAdapter comingSoonAdapter;
    private NowPlayingMoviesAdapter nowPlayingAdapter;
    private FeaturedAdapter featuredAdapter;
    MoviesResponse moviesResponse;
    SearchFragment searchFrag = new SearchFragment();
    ImageView movieLogo, searchicon;
    Context myContext;
    RealmList<Movie> TopBoxOffice;
    RealmList<Movie> comingSoon;
    RealmList<Movie> NEWRelease;
    RealmList<Movie> featured;
    RealmList<Movie> nowPlaying;
    View preview;
    LockableScrollView lockableScrollView;
    TextView previewMovieTitle, previewSynopsis;
    SimpleDraweeView previewMovieImage;
    TextView previewRating, previewRunningTime;
    View parent;
    ImageView background;


    private searchMoviesInterface searchMoviesInterface;

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

    public RealmConfiguration config;

    View progress;
    private RealmConfiguration allMoviesConfig;
    private CustomLinearLayoutManager newReleasesLayoutManager;
    private CustomLinearLayoutManager topBoxOfficeLayoutManager;
    private CustomLinearLayoutManager comingSoonLayoutManager;
    private CustomLinearLayoutManager featuredManager;
    private CustomLinearLayoutManager nowplayingManager;


    public static MoviesFragment newInstance() {
        return new MoviesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        ButterKnife.bind(this, rootView);
        swiper = rootView.findViewById(R.id.SWIPE2REFRESH);
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
        preview = rootView.findViewById(R.id.preview);
        lockableScrollView = rootView.findViewById(R.id.MOVIES_MAINCONTENT);
        previewMovieImage = rootView.findViewById(R.id.previewImage);
        previewMovieTitle = rootView.findViewById(R.id.previewMovieTitle);
        previewSynopsis = rootView.findViewById(R.id.previewSynopsis);
        previewRunningTime = rootView.findViewById(R.id.previewRunningTime);
        previewRating = rootView.findViewById(R.id.previewRating);
        parent = rootView.findViewById(R.id.parent);
        background = rootView.findViewById(R.id.background);
        Api api;
        NEWRelease = new RealmList<>();
        TopBoxOffice = new RealmList<>();
        comingSoon = new RealmList<>();
        featured = new RealmList<>();
        nowPlaying = new RealmList<>();


        int resId = R.anim.layout_animation;
        int res2 = R.anim.layout_anim_bottom;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(myActivity, resId);
        LayoutAnimationController animation2 = AnimationUtils.loadLayoutAnimation(myActivity, res2);

        /** New Releases RecyclerView */
        newReleasesLayoutManager = new CustomLinearLayoutManager(myActivity, LinearLayoutManager.HORIZONTAL, false);


        newReleasesRecycler = rootView.findViewById(R.id.new_releases);
        newReleasesRecycler.setLayoutManager(newReleasesLayoutManager);
        newReleasesRecycler.setItemAnimator(null);
        fadeIn(newReleasesRecycler);
        newReleasesRecycler.setLayoutAnimation(animation);
        newRealeasesAdapter = new MoviesNewReleasesAdapter(myActivity, NEWRelease, this);

        /** Top Box Office RecyclerView */
        topBoxOfficeLayoutManager = new CustomLinearLayoutManager(myActivity, LinearLayoutManager.HORIZONTAL, false);

        topBoxOfficeRecycler = rootView.findViewById(R.id.top_box_office);
        topBoxOfficeRecycler.setLayoutManager(topBoxOfficeLayoutManager);
        topBoxOfficeRecycler.setItemAnimator(null);
        fadeIn(topBoxOfficeRecycler);
        topBoxOfficeRecycler.setLayoutAnimation(animation);

        topBoxOfficeAdapter = new MoviesTopBoxOfficeAdapter(myActivity, TopBoxOffice, this);

        /** Coming Soon RecyclerView */
        comingSoonLayoutManager = new CustomLinearLayoutManager(myActivity, LinearLayoutManager.HORIZONTAL, false);
        comingSoonRecycler = rootView.findViewById(R.id.coming_soon);
        comingSoonRecycler.setLayoutManager(comingSoonLayoutManager);
        comingSoonRecycler.setItemAnimator(null);
        fadeIn(comingSoonRecycler);
        comingSoonRecycler.setLayoutAnimation(animation);

        comingSoonAdapter = new MoviesComingSoonAdapter(myActivity, comingSoon, this);

        /** NOW PLAYING */
        nowplayingManager = new CustomLinearLayoutManager(myActivity, LinearLayoutManager.HORIZONTAL, false);
        nowPlayingRecycler = rootView.findViewById(R.id.now_playing);
        nowPlayingRecycler.setLayoutManager(nowplayingManager);
        fadeIn(nowPlayingRecycler);
        nowPlayingRecycler.setLayoutAnimation(animation);

        nowPlayingAdapter = new NowPlayingMoviesAdapter(myActivity, nowPlaying, this);

        /** FEATURED */
        featuredManager = new CustomLinearLayoutManager(myActivity, LinearLayoutManager.HORIZONTAL, false);
        featuredRecycler = rootView.findViewById(R.id.FeaturedRE);
        featuredRecycler.setLayoutManager(featuredManager);
        fadeIn(featuredRecycler);
        featuredAdapter = new FeaturedAdapter(myActivity, featured, this);
        featuredRecycler.setLayoutAnimation(animation2);


        /** SEARCH */
        searchicon.setOnClickListener(view -> {

            searchMoviesInterface.onSearchMoviesInterface();
        });


        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        LocationManager = (LocationManager) myActivity.getSystemService(Context.LOCATION_SERVICE);
        if (LocationManager != null) {
            Provider = LocationManager.getBestProvider(criteria, true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        progress.setVisibility(View.VISIBLE);


        LogUtils.newLog(Constants.TAG, "ANDROID ID >>>>>>>>>>>>>: " + Settings.Secure.getString(myContext.getContentResolver(), Settings.Secure.ANDROID_ID));
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        config = new RealmConfiguration.Builder()
                .name("Movies.Realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        historyConfig = new RealmConfiguration.Builder()
                .name("History.Realm")
                .deleteRealmIfMigrationNeeded()
                .build();

        moviesRealm = Realm.getInstance(config);
        TheatersFragment.tRealm = Realm.getDefaultInstance();
        historyRealm = Realm.getInstance(historyConfig);

        swiper.setOnRefreshListener(() -> {

            myActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            moviesRealm.executeTransaction(realm -> {
                realm.deleteAll();
            });
            getMoviesForStorage();
//            GoWatchItSingleton.getInstance().getMovies();
        });


        if (moviesRealm.isEmpty()) {
            getMoviesForStorage();
            getAllMovies();
        } else {
            setAdaptersWithRealmOBjects();
        }

        if (historyRealm.isEmpty()) {
            getHistoryForStorage();
        }
        comingSoonLayoutManager.setScrollEnabled(true);
        newReleasesLayoutManager.setScrollEnabled(true);
        topBoxOfficeLayoutManager.setScrollEnabled(true);
        nowplayingManager.setScrollEnabled(true);
        lockableScrollView.setScrollingEnabled(true);

    }

    void getAllMovies() {
        LogUtils.newLog(Constants.TAG, "getAllMovies: GETTING ALL MOVIES");
        allMoviesConfig = new RealmConfiguration.Builder()
                .name("AllMovies.Realm")
                .deleteRealmIfMigrationNeeded()
                .build();


        allMoviesRealm = Realm.getInstance(allMoviesConfig);
        RestClient.getLocalStorageAPI().getAllMovies().enqueue(new Callback<List<AllMoviesResponse>>() {
            @Override
            public void onResponse(Call<List<AllMoviesResponse>> call, Response<List<AllMoviesResponse>> response) {
                List<AllMoviesResponse> info = new ArrayList<>();
                info = response.body();
                if (response.isSuccessful() && response != null) {
                    List<AllMoviesResponse> finalInfo = info;
                    allMoviesRealm.executeTransaction(realm -> {
                        for (AllMoviesResponse movie : finalInfo) {
                            Movie newMovie = realm.createObject(Movie.class);
                            newMovie.setId(Integer.parseInt(movie.getId()));
                            newMovie.setTitle(movie.getTitle());
                            newMovie.setRunningTime(Integer.parseInt(movie.getRunningTime()));
                            newMovie.setRating(movie.getRating());
                            newMovie.setSynopsis(movie.getSynopsis());
                            newMovie.setImageUrl(movie.getImageUrl());
                            newMovie.setLandscapeImageUrl(movie.getLandscapeImageUrl());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<AllMoviesResponse>> call, Throwable t) {
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Request location updates:
                Toast.makeText(myActivity, "GPS Location Is Required", Toast.LENGTH_SHORT).show();
            }
        }

        config = new RealmConfiguration.Builder()
                .name("Movies.Realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        moviesRealm = Realm.getInstance(config);
        TheatersFragment.tRealm = Realm.getDefaultInstance();

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof searchMoviesInterface) {
            searchMoviesInterface = (searchMoviesInterface) context;
        }
        myContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        myContext = null;
        searchMoviesInterface = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        moviesRealm.close();
    }

    //TODO:
    public void onMoviePosterClick(int pos, Movie movie, ImageView sharedImageView) {
        Intent movieIntent = new Intent(myActivity, MovieActivity.class);
        movieIntent.putExtra(MovieActivity.MOVIE, Parcels.wrap(movie));
        startActivity(movieIntent);
    }

    public void getMoviesForStorage() {
        RestClient.getLocalStorageAPI().getAllCurrentMovies().enqueue(new Callback<LocalStorageMovies>() {
            @Override
            public void onResponse(Call<LocalStorageMovies> call, Response<LocalStorageMovies> response) {
                LocalStorageMovies localStorageMovies = response.body();
                if (response.isSuccessful() && localStorageMovies != null) {

                    moviesRealm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            for (int i = 0; i < localStorageMovies.getNewReleases().size(); i++) {
                                Movie newReleaseMovies = realm.createObject(Movie.class);
                                newReleaseMovies.setType("New Releases");
                                newReleaseMovies.setId(localStorageMovies.getNewReleases().get(i).getId());
                                newReleaseMovies.setRunningTime(localStorageMovies.getNewReleases().get(i).getRunningTime());
                                newReleaseMovies.setSynopsis(localStorageMovies.getNewReleases().get(i).getSynopsis());
                                newReleaseMovies.setImageUrl(localStorageMovies.getNewReleases().get(i).getImageUrl());
                                newReleaseMovies.setLandscapeImageUrl(localStorageMovies.getNewReleases().get(i).getLandscapeImageUrl());
                                newReleaseMovies.setTheaterName(localStorageMovies.getNewReleases().get(i).getTheaterName());
                                newReleaseMovies.setTitle(localStorageMovies.getNewReleases().get(i).getTitle());
                                newReleaseMovies.setTribuneId(localStorageMovies.getNewReleases().get(i).getTribuneId());
                                newReleaseMovies.setRating(localStorageMovies.getNewReleases().get(i).getRating());
                                newReleaseMovies.setTeaserVideoUrl(localStorageMovies.getNewReleases().get(i).getTeaserVideoUrl());


                            }
                            for (int i = 0; i < localStorageMovies.getNowPlaying().size(); i++) {
                                Movie nowPlayingMovies = realm.createObject(Movie.class);
                                nowPlayingMovies.setType("Now Playing");
                                nowPlayingMovies.setId(localStorageMovies.getNowPlaying().get(i).getId());
                                nowPlayingMovies.setRunningTime(localStorageMovies.getNowPlaying().get(i).getRunningTime());
                                nowPlayingMovies.setSynopsis(localStorageMovies.getNowPlaying().get(i).getSynopsis());
                                nowPlayingMovies.setImageUrl(localStorageMovies.getNowPlaying().get(i).getImageUrl());
                                nowPlayingMovies.setLandscapeImageUrl(localStorageMovies.getNowPlaying().get(i).getLandscapeImageUrl());
                                nowPlayingMovies.setTheaterName(localStorageMovies.getNowPlaying().get(i).getTheaterName());
                                nowPlayingMovies.setTitle(localStorageMovies.getNowPlaying().get(i).getTitle());
                                nowPlayingMovies.setTribuneId(localStorageMovies.getNowPlaying().get(i).getTribuneId());
                                nowPlayingMovies.setRating(localStorageMovies.getNowPlaying().get(i).getRating());
                                nowPlayingMovies.setTeaserVideoUrl(localStorageMovies.getNowPlaying().get(i).getTeaserVideoUrl());


                            }
                            for (int i = 0; i < localStorageMovies.getFeatured().size(); i++) {
                                Movie featuredMovie = realm.createObject(Movie.class);
                                featuredMovie.setType("Featured");
                                featuredMovie.setId(localStorageMovies.getFeatured().get(i).getId());
                                featuredMovie.setRunningTime(localStorageMovies.getFeatured().get(i).getRunningTime());
                                featuredMovie.setSynopsis(localStorageMovies.getFeatured().get(i).getSynopsis());
                                featuredMovie.setImageUrl(localStorageMovies.getFeatured().get(i).getImageUrl());
                                featuredMovie.setLandscapeImageUrl(localStorageMovies.getFeatured().get(i).getLandscapeImageUrl());
                                featuredMovie.setTheaterName(localStorageMovies.getFeatured().get(i).getTheaterName());
                                featuredMovie.setTitle(localStorageMovies.getFeatured().get(i).getTitle());
                                featuredMovie.setTribuneId(localStorageMovies.getFeatured().get(i).getTribuneId());
                                featuredMovie.setRating(localStorageMovies.getFeatured().get(i).getRating());
                                featuredMovie.setReleaseDate(localStorageMovies.getFeatured().get(i).getReleaseDate());
                                featuredMovie.setTeaserVideoUrl(localStorageMovies.getFeatured().get(i).getTeaserVideoUrl());
                                featuredMovie.setCreatedAt(localStorageMovies.getFeatured().get(i).getCreatedAt());


                            }


                            for (int i = 0; i < localStorageMovies.getComingSoon().size(); i++) {
                                Movie comingSoonMovies = realm.createObject(Movie.class);
                                comingSoonMovies.setType("Coming Soon");
                                comingSoonMovies.setId(localStorageMovies.getComingSoon().get(i).getId());
                                comingSoonMovies.setRunningTime(localStorageMovies.getComingSoon().get(i).getRunningTime());
                                comingSoonMovies.setSynopsis(localStorageMovies.getComingSoon().get(i).getSynopsis());
                                comingSoonMovies.setImageUrl(localStorageMovies.getComingSoon().get(i).getImageUrl());
                                comingSoonMovies.setLandscapeImageUrl(localStorageMovies.getComingSoon().get(i).getLandscapeImageUrl());
                                comingSoonMovies.setTheaterName(localStorageMovies.getComingSoon().get(i).getTheaterName());
                                comingSoonMovies.setTitle(localStorageMovies.getComingSoon().get(i).getTitle());
                                comingSoonMovies.setTribuneId(localStorageMovies.getComingSoon().get(i).getTribuneId());
                                comingSoonMovies.setCreatedAt(localStorageMovies.getComingSoon().get(i).getCreatedAt());
                                comingSoonMovies.setRating(localStorageMovies.getComingSoon().get(i).getRating());
                                comingSoonMovies.setReleaseDate(localStorageMovies.getComingSoon().get(i).getReleaseDate());
                                comingSoonMovies.setTeaserVideoUrl(localStorageMovies.getComingSoon().get(i).getTeaserVideoUrl());

                            }
                            for (int i = 0; i < localStorageMovies.getTopBoxOffice().size(); i++) {
                                Movie topBoxOfficeMovies = realm.createObject(Movie.class);
                                topBoxOfficeMovies.setType("Top Box Office");
                                topBoxOfficeMovies.setId(localStorageMovies.getTopBoxOffice().get(i).getId());
                                topBoxOfficeMovies.setRunningTime(localStorageMovies.getTopBoxOffice().get(i).getRunningTime());
                                topBoxOfficeMovies.setSynopsis(localStorageMovies.getTopBoxOffice().get(i).getSynopsis());
                                topBoxOfficeMovies.setImageUrl(localStorageMovies.getTopBoxOffice().get(i).getImageUrl());
                                topBoxOfficeMovies.setLandscapeImageUrl(localStorageMovies.getTopBoxOffice().get(i).getLandscapeImageUrl());
                                topBoxOfficeMovies.setTheaterName(localStorageMovies.getTopBoxOffice().get(i).getTheaterName());
                                topBoxOfficeMovies.setTitle(localStorageMovies.getTopBoxOffice().get(i).getTitle());
                                topBoxOfficeMovies.setTribuneId(localStorageMovies.getTopBoxOffice().get(i).getTribuneId());
                                topBoxOfficeMovies.setRating(localStorageMovies.getTopBoxOffice().get(i).getRating());
                                topBoxOfficeMovies.setTeaserVideoUrl(localStorageMovies.getTopBoxOffice().get(i).getTeaserVideoUrl());
                            }
                        }
                    }, () -> {
                        LogUtils.newLog(Constants.TAG, "onSuccess: ");
                        setAdaptersWithRealmOBjects();
                    }, error -> {
                        LogUtils.newLog(Constants.TAG, "onResponse: " + error.getMessage());
                    });

                    swiper.setRefreshing(false);
                }

            }

            @Override
            public void onFailure(Call<LocalStorageMovies> call, Throwable t) {
                Toast.makeText(myActivity, "Failure Updating Movies", Toast.LENGTH_SHORT).show();
                swiper.setRefreshing(false);
                ActivityCompat.requestPermissions(myActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSIONS);
            }
        });
    }

    void getHistoryForStorage() {

        RestClient.getAuthenticated().getReservations().enqueue(new Callback<HistoryResponse>() {
            @Override
            public void onResponse(Call<HistoryResponse> call, Response<HistoryResponse> response) {
                if (response.isSuccessful()) {
                    HistoryResponse historyObjects = response.body();
                    historyRealm.executeTransactionAsync(realm -> {
                        if (historyObjects != null) {
                            Calendar lastMonthYear = Calendar.getInstance();
                            lastMonthYear.add(Calendar.MONTH, -1);
                            int year = lastMonthYear.get(Calendar.YEAR);
                            int lastMonth = lastMonthYear.get(Calendar.MONTH) + 1;
                            int lastMonthCount = 0;
                            Movie newest = historyObjects.getReservations().size() > 0 ? historyObjects.getReservations().get(0) : null;
                            for (int i = 0; i < historyObjects.getReservations().size(); i++) {
                                Movie movieReservation = historyObjects.getReservations().get(i);
                                Movie historyList = realm.createObject(Movie.class);
                                historyList.setId(movieReservation.getId());
                                historyList.setCreatedAt(movieReservation.getCreatedAt());
                                historyList.setImageUrl(movieReservation.getImageUrl());
                                historyList.setRating(movieReservation.getRating());
                                historyList.setReleaseDate(movieReservation.getReleaseDate());
                                historyList.setRunningTime(movieReservation.getRunningTime());
                                historyList.setTheaterName(movieReservation.getTheaterName());
                                historyList.setTitle(movieReservation.getTitle());
                                historyList.setTribuneId(movieReservation.getTribuneId());
                                historyList.setType(movieReservation.getType());
                                if (movieReservation.getUserRating() != null) {
                                    Log.d(Constants.TAG, "GET USER RATING: " + movieReservation.getUserRating());
                                    historyList.setUserRating(movieReservation.getUserRating());
                                }

                                Calendar cal = Calendar.getInstance();
                                cal.setTimeInMillis(movieReservation.getCreatedAt());
                                int movieSeenYear = cal.get(Calendar.YEAR);
                                int movieSeenMonth = cal.get(Calendar.MONTH);
                                if (movieSeenMonth == lastMonth && movieSeenYear == year) {
                                    lastMonthCount++;
                                }
                                if (movieReservation.getCreatedAt() > newest.getCreatedAt()) {
                                    newest = movieReservation;
                                }
                            }
                            UserPreferences.setTotalMoviesSeenLastMonth(lastMonthCount);
                            UserPreferences.setTotalMoviesSeen(historyObjects.getReservations().size());
                            if (newest != null) {
                                UserPreferences.setLastMovieSeen(newest);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<HistoryResponse> call, Throwable t) {
            }
        });
    }


    public void setAdaptersWithRealmOBjects() {

        TopBoxOffice.clear();
        comingSoon.clear();
        NEWRelease.clear();
        nowPlaying.clear();
        featured.clear();

        topBoxTXT.setVisibility(View.VISIBLE);
        fadeIn(topBoxTXT);
        comingSoonTXT.setVisibility(View.VISIBLE);
        fadeIn(comingSoonTXT);
        newReleaseTXT.setVisibility(View.VISIBLE);
        fadeIn(newReleaseTXT);
        nowPlayingTXT.setVisibility(View.VISIBLE);
        fadeIn(nowPlayingTXT);


        RealmResults<Movie> allMovies = moviesRealm.where(Movie.class)
                .equalTo("type", "Top Box Office")
                .or()
                .equalTo("type", "New Releases")
                .or()
                .equalTo("type", "Coming Soon")
                .or()
                .equalTo("type", "Now Playing")
                .or()
                .equalTo("type", "Featured")
                .findAll();

        LogUtils.newLog(Constants.TAG, "setAdaptersWithRealmOBjects: " + allMovies.size());
        for (int i = 0; i < allMovies.size(); i++) {

            if (allMovies.get(i).getType().matches("New Releases")) {
                NEWRelease.add(allMovies.get(i));
            } else if (allMovies.get(i).getType().matches("Coming Soon")) {
                Collections.sort(comingSoon, (o1, o2) -> o1.getReleaseDate().compareTo(o2.getReleaseDate()));
                comingSoon.add(allMovies.get(i));
            } else if (allMovies.get(i).getType().matches("Now Playing")) {
                nowPlaying.add(allMovies.get(i));
            } else if (allMovies.get(i).getType().matches("Featured")) {
                featured.add(allMovies.get(i));
            } else if (allMovies.get(i).getType().matches("Top Box Office")) {
                TopBoxOffice.add(allMovies.get(i));
            }

        }

        if (newRealeasesAdapter != null) {
            newReleasesRecycler.getRecycledViewPool().clear();
            newReleasesRecycler.setAdapter(newRealeasesAdapter);
            newRealeasesAdapter.notifyDataSetChanged();

        }

        if (topBoxOfficeAdapter != null) {
            topBoxOfficeRecycler.getRecycledViewPool().clear();
            topBoxOfficeRecycler.setAdapter(topBoxOfficeAdapter);
            topBoxOfficeAdapter.notifyDataSetChanged();
        }

        if (comingSoonAdapter != null) {
            comingSoonRecycler.getRecycledViewPool().clear();
            comingSoonRecycler.setAdapter(comingSoonAdapter);
            comingSoonAdapter.notifyDataSetChanged();
        }

        if (nowPlayingAdapter != null) {
            nowPlayingRecycler.getRecycledViewPool().clear();
            nowPlayingRecycler.setAdapter(nowPlayingAdapter);
            nowPlayingAdapter.notifyDataSetChanged();
        }

        if (featuredAdapter != null) {
            featuredRecycler.getRecycledViewPool().clear();
            featuredRecycler.setAdapter(featuredAdapter);
            featuredAdapter.notifyDataSetChanged();
        }

        searchicon.setVisibility(View.VISIBLE);
        fadeIn(searchicon);

        progress.setVisibility(View.GONE);
        myActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myActivity = activity;

    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(myActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
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
                                ActivityCompat.requestPermissions(myActivity,
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
                    Toast.makeText(myActivity, "GPS Permissions Are Required. Go To App Settings.", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onMoviePosterLongClick(int pos, @NotNull Movie movie, @NotNull ImageView sharedImageView) {
        Bitmap bitmap = getBitmapFromView(parent);
        preview.setVisibility(View.VISIBLE);
        comingSoonLayoutManager.setScrollEnabled(false);
        newReleasesLayoutManager.setScrollEnabled(false);
        topBoxOfficeLayoutManager.setScrollEnabled(false);
        nowplayingManager.setScrollEnabled(false);
        lockableScrollView.setScrollingEnabled(false);
        lockableScrollView.setVisibility(View.GONE);
        swiper.setEnabled(false);


        final Uri imgUrl = Uri.parse(movie.getImageUrl());
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                .setProgressiveRenderingEnabled(true)
                .setSource(imgUrl)
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(previewMovieImage.getController())
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);

                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        previewMovieImage.setImageURI(imgUrl + "/original.jpg");

                    }
                })
                .build();

        if (imgUrl.toString().contains("default")) {
            previewMovieImage.refreshDrawableState();
        }
        previewMovieImage.setController(controller);

        previewMovieTitle.setText(movie.getTitle());
        previewSynopsis.setText(movie.getSynopsis());


        int t = movie.getRunningTime();
        int hours = t / 60;
        int minutes = t % 60;

        if (t == 0) {
            previewRunningTime.setVisibility(View.GONE);
        } else if (hours > 1) {
            String translatedRunTime = hours + " hours " + minutes + " minutes";
            previewRunningTime.setText(translatedRunTime);
        } else {
            String translatedRunTime = hours + " hour " + minutes + " minutes";
            previewRunningTime.setText(translatedRunTime);
        }


        previewRating.setText("Rated: " + movie.getRating());


        RenderScript rs = RenderScript.create(parent.getContext());
        RSBlurProcessor rsBlurProcessor = new RSBlurProcessor(rs);
        Bitmap finalBitmap = rsBlurProcessor.blur(bitmap, 200f, 4);

        background.setImageBitmap(finalBitmap);
        background.setVisibility(View.VISIBLE);

    }


    public static Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        view.draw(c);
        return bitmap;
    }

    @Override
    public void releaseLongPress() {
        preview.setVisibility(View.GONE);
        comingSoonLayoutManager.setScrollEnabled(true);
        newReleasesLayoutManager.setScrollEnabled(true);
        topBoxOfficeLayoutManager.setScrollEnabled(true);
        nowplayingManager.setScrollEnabled(true);
        lockableScrollView.setScrollingEnabled(true);
        lockableScrollView.setVisibility(View.VISIBLE);
        swiper.setEnabled(true);
        background.setVisibility(View.GONE);
    }


    public interface searchMoviesInterface {
        void onSearchMoviesInterface();

        void closeFragment();

        void hideSnackBar();

        void showSnackbar();

    }


}





