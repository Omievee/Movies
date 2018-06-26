package com.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.mobile.Constants;
import com.mobile.MoviePosterClickListener;
import com.mobile.UserPreferences;
import com.mobile.activities.ActivateMoviePassCard;
import com.mobile.adapters.DynamicMoviesTabAdapter;
import com.mobile.featured.FeaturedMovieAdapter;
import com.mobile.helpers.GoWatchItSingleton;
import com.mobile.helpers.LogUtils;
import com.mobile.history.HistoryManager;
import com.mobile.home.RestrictionsManager;
import com.mobile.model.Movie;
import com.mobile.network.RestClient;
import com.mobile.responses.AllMoviesResponse;
import com.mobile.responses.LocalStorageMovies;
import com.mobile.responses.MicroServiceRestrictionsResponse;
import com.moviepass.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.disposables.Disposable;
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

public class MoviesFragment extends MPFragment implements MoviePosterClickListener, MoviesView {

    @Inject
    MoviesFragmentPresenter presenter;

    @Inject
    RestrictionsManager restrictionsManager;

    @Inject
    HistoryManager historyManager;
    //Tag
    public static final String TAG = "MoviesFragment";

    public static final String MOVIE = "movie";

    //Realm
    public static Realm moviesRealm;
    private Realm allMoviesRealm;
    private RealmList<Movie> TopBoxOffice;
    private RealmList<Movie> comingSoon;
    private RealmList<Movie> NEWRelease;
    private RealmList<Movie> featured;
    private RealmList<Movie> nowPlaying;
    public RealmConfiguration config;
    private RealmConfiguration allMoviesConfig;
    private Activity activity;
    Realm historyRealm;
    RealmConfiguration historyConfig;

    //Recycler View Adapters
    private FeaturedMovieAdapter featuredAdapter;
    //Views
    public static SwipeRefreshLayout swiper;
    private ImageView movieLogo, searchicon;
    private View progress;
    private View parentLayout;
    private ScrollView scrollView;
    private ViewGroup toolBar;
    private Guideline guideLineLeft;
    private Guideline guideLineRight;
    public ImageView toolBarBackground;
    public Button activateMPCardButton;

    //Location
    public static final int LOCATION_PERMISSIONS = 99;
    //Java Objects
    private String Provider;

    @BindView(R.id.FeaturedRE)
    RecyclerView featuredRecycler;

    RecyclerView recyclerView;
    FrameLayout childFragment;
    ArrayList<Fragment> backStack;
    public static FragmentManager childFragmentManager;


    boolean scrolldown, scrollup;
    int movieId;


    public static MoviesFragment newInstance(int movieId) {
        MoviesFragment fragment = new MoviesFragment();
        Bundle args = new Bundle();
        args.putInt(MOVIE, movieId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            movieId = getArguments().getInt(MOVIE);
        } else {
            movieId = -1;
        }
    }

    public MoviesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    /**
     * Setting up all views
     */
    public void setUpViews(View rootView) {
        backStack = new ArrayList<>();
        swiper = rootView.findViewById(R.id.SWIPE2REFRESH);
        progress = rootView.findViewById(R.id.progress);
        recyclerView = rootView.findViewById(R.id.moviesRecyclerView);
        searchicon = rootView.findViewById(R.id.search_inactive);
        searchicon.setVisibility(View.GONE);
        movieLogo = rootView.findViewById(R.id.MoviePass_HEADER);
        childFragment = rootView.findViewById(R.id.frame_layout);
        scrollView = rootView.findViewById(R.id.MOVIES_MAINCONTENT);
        toolBar = rootView.findViewById(R.id.toolBar);
        guideLineLeft = rootView.findViewById(R.id.guideline);
        guideLineRight = rootView.findViewById(R.id.guideline1);
        toolBarBackground = rootView.findViewById(R.id.toolBarBackground);
        scrollup = false;
        scrolldown = false;
        activateMPCardButton = rootView.findViewById(R.id.bottomButton);
    }

    /**
     * Setting Up Recycler Views
     * Setting Up Realm
     */
    public void setUpMoviesList(View rootView) {
        NEWRelease = new RealmList<>();
        TopBoxOffice = new RealmList<>();
        comingSoon = new RealmList<>();
        featured = new RealmList<>();
        nowPlaying = new RealmList<>();


        LinearLayoutManager newReleasesLayoutManager = new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(newReleasesLayoutManager);

        /** FEATURED */
        LinearLayoutManager featuredManager = new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false);
        featuredRecycler = rootView.findViewById(R.id.FeaturedRE);
        featuredRecycler.setLayoutManager(featuredManager);
        fadeIn(featuredRecycler);
        featuredAdapter = new FeaturedMovieAdapter(featured, this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpViews(view);
        progress.setVisibility(View.VISIBLE);

        /** SEARCH */
        searchicon.setOnClickListener(v -> showFragment(new SearchFragment()));
        childFragmentManager = getChildFragmentManager();
        config = new RealmConfiguration.Builder()
                .name("Movies.Realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        historyConfig = new RealmConfiguration.Builder()
                .name("History.Realm")
                .deleteRealmIfMigrationNeeded()
                .build();

        moviesRealm = Realm.getInstance(config);
        historyRealm = Realm.getInstance(historyConfig);
        setUpMoviesList(view);

        swiper.setOnRefreshListener(() -> {

            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            moviesRealm.executeTransaction(realm -> {
                realm.deleteAll();
            });
            getMoviesForStorage();
            GoWatchItSingleton.getInstance().getMovies();
        });

        if (moviesRealm.isEmpty()) {
            getMoviesForStorage();
            getAllMovies();
        } else {
            setAdaptersWithRealmOBjects();
        }



        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) toolBar.getLayoutParams();
        params.setMargins(0, getStatusBarHeight(), 0, 0);
        toolBar.setLayoutParams(params);
        toolBarBackground.setLayoutParams(params);


        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (scrollView.getScrollY() >= 150 && !scrolldown) {
                    scrolldown = true;
                    scrollup = false;
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guideLineLeft.getLayoutParams();
                    params.guidePercent = 0.35f; // 45% // range: 0 <-> 1
                    guideLineLeft.setLayoutParams(params);

                    ConstraintLayout.LayoutParams params1 = (ConstraintLayout.LayoutParams) guideLineRight.getLayoutParams();
                    params1.guidePercent = 0.65f; // 45% // range: 0 <-> 1


                    toolBarBackground.animate().alpha(1.0f);
                    TransitionManager.beginDelayedTransition(toolBar);
                    guideLineRight.setLayoutParams(params1);

                }
                if (scrollView.getScrollY() < 150 && !scrollup) {
                    scrollup = true;
                    scrolldown = false;


                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guideLineLeft.getLayoutParams();
                    params.guidePercent = 0.3f; // 45% // range: 0 <-> 1
                    guideLineLeft.setLayoutParams(params);

                    ConstraintLayout.LayoutParams params1 = (ConstraintLayout.LayoutParams) guideLineRight.getLayoutParams();
                    params1.guidePercent = 0.7f; // 45% // range: 0 <-> 1
                    TransitionManager.beginDelayedTransition(toolBar);
                    toolBarBackground.animate().alpha(0.0f);
                    guideLineRight.setLayoutParams(params1);

                }
            }
        });
        subscribeToRestrictions();
    }

    @Nullable
    Disposable restritionSub;

    private void subscribeToRestrictions() {
        if (restritionSub != null) {
            restritionSub.dispose();
        }
        restritionSub = restrictionsManager
                .payload()
                .subscribe(this::showSubscriptionButton, err -> {
                });
    }


    private void showSubscriptionButton(MicroServiceRestrictionsResponse res) {
        Log.d(TAG, "showSubscriptionButton: " + res.getSubscriptionActivationRequired());
        if (res.getSubscriptionActivationRequired()) {
            activateMPCardButton.setVisibility(View.VISIBLE);
            activateMPCardButton.setClickable(true);
            activateMPCardButton.setOnClickListener(v -> {
                Intent activateCard = new Intent(getContext(), ActivateMoviePassCard.class);
                startActivity(activateCard);
            });
        } else {
            activateMPCardButton.setVisibility(View.GONE);
        }
    }


    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
        activity = getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        moviesRealm.close();
    }

    public void onMoviePosterClick(Movie movie) {
        if (movie == null || !movie.isValid()) {
        } else {
            showFragment(MovieFragment.newInstance(movie));
        }
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
                                featuredMovie.setTeaserVideoUrl(localStorageMovies.getFeatured().get(i).getTeaserVideoUrl());
                                featuredMovie.setCreatedAt(localStorageMovies.getFeatured().get(i).getCreatedAt());
                                featuredMovie.setReleaseDate(localStorageMovies.getFeatured().get(i).getReleaseDate());
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
                        moviesRealm.close();
                    }, error -> {
                        LogUtils.newLog(Constants.TAG, "onResponse: " + error.getMessage());
                        moviesRealm.close();
                    });
                    swiper.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<LocalStorageMovies> call, Throwable t) {
                Toast.makeText(getContext(), "Failure Updating Movies", Toast.LENGTH_SHORT).show();
                swiper.setRefreshing(false);
            }
        });
    }


    void getAllMovies() {
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
                    allMoviesRealm.executeTransactionAsync(realm -> {
                        Log.d(TAG, "onResponse: ");
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
                    }, () -> {
                        LogUtils.newLog(Constants.TAG, "onSuccess: ");
                    }, error -> {
                        // Transaction failed and was automatically canceled.
                    });
                }

            }


            @Override
            public void onFailure(Call<List<AllMoviesResponse>> call, Throwable t) {
            }
        });
    }

    public void getMovie(int movieId) {
        Log.d(TAG, "getMovie: " + movieId);

        RealmResults<Movie> movie = moviesRealm.where(Movie.class)
                .equalTo("id", movieId)
                .findAll();

        if (movie != null && movie.size() > 0) {
            showFragment(MovieFragment.newInstance(movie.get(0)));
        }
        progress.setVisibility(View.GONE);
    }


    public void setAdaptersWithRealmOBjects() {
        TopBoxOffice.clear();
        comingSoon.clear();
        NEWRelease.clear();
        nowPlaying.clear();
        featured.clear();

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

        ArrayList<RealmList<Movie>> moviesList = new ArrayList<>();
        moviesList.add(NEWRelease);
        moviesList.add(nowPlaying);
        moviesList.add(TopBoxOffice);
        moviesList.add(comingSoon);

        ArrayList<String> titlesList = new ArrayList<>();
        titlesList.add("New Releases");
        titlesList.add("Now Playing");
        titlesList.add("Top Box Office");
        titlesList.add("Coming Soon");

        DynamicMoviesTabAdapter adapter = new DynamicMoviesTabAdapter(titlesList, moviesList, this);
        recyclerView.setAdapter(adapter);
        if (featuredAdapter != null) {
            featuredRecycler.getRecycledViewPool().clear();
            featuredRecycler.setAdapter(featuredAdapter);
            featuredAdapter.notifyDataSetChanged();
        }

        searchicon.setVisibility(View.VISIBLE);
        fadeIn(searchicon);

        if (movieId == -1) {
            progress.setVisibility(View.GONE);
        } else {
            getMovie(movieId);
        }
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

    }


}




