package com.moviepass.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.view.ViewCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.moviepass.MoviePosterClickListener;
import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.activities.MovieActivity;
import com.moviepass.adapters.MoviesComingSoonAdapter;
import com.moviepass.adapters.MoviesNewReleasesAdapter;
import com.moviepass.adapters.MoviesTopBoxOfficeAdapter;
import com.moviepass.model.Movie;
import com.moviepass.model.MoviesResponse;
import com.moviepass.network.RestClient;

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

public class MoviesFragment extends Fragment implements MoviePosterClickListener {

    public static final String MOVIES = "movies";
    public static final String EXTRA_MOVIE_IMAGE_TRANSITION_NAME = "movie_image_transition_name";
    public static final String EXTRA_MOVIE_ITEM = "movie_image_url";


    private MoviesNewReleasesAdapter mMoviesNewReleasesAdapter;
    private MoviesTopBoxOfficeAdapter mMoviesTopBoxOfficeAdapter;
    private MoviesComingSoonAdapter mMoviesComingSoonAdapter;

    MoviesFragment mMoviesFragment;
    MoviesResponse mMoviesResponse;
    ArrayList<Movie> mMovieArrayList;

    ArrayList<Movie> mMoviesTopBoxOffice;
    ArrayList<Movie> mMoviesComingSoon;
    ArrayList<Movie> mMoviesNewReleases;

    @BindView(R.id.new_releases)
    RecyclerView mNewReleasesRecyclerView;
    @BindView(R.id.top_box_office)
    RecyclerView mTopBoxOfficeRecyclerView;
    @BindView(R.id.coming_soon)
    RecyclerView mComingSoonRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        ButterKnife.bind(this, rootView);

        mMoviesNewReleases = new ArrayList<>();
        mMoviesTopBoxOffice = new ArrayList<>();
        mMoviesComingSoon = new ArrayList<>();

        /* New Releases RecyclerView */
        LinearLayoutManager newReleasesLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        mNewReleasesRecyclerView = (RecyclerView) rootView.findViewById(R.id.new_releases);
        mNewReleasesRecyclerView.setLayoutManager(newReleasesLayoutManager);

        mMoviesNewReleasesAdapter = new MoviesNewReleasesAdapter(mMoviesNewReleases, this);


        /* Top Box Office RecyclerView */
        LinearLayoutManager topBoxOfficeLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        mTopBoxOfficeRecyclerView = (RecyclerView) rootView.findViewById(R.id.top_box_office);
        mTopBoxOfficeRecyclerView.setLayoutManager(topBoxOfficeLayoutManager);

        mMoviesTopBoxOfficeAdapter = new MoviesTopBoxOfficeAdapter(mMoviesTopBoxOffice, this);

        /* Coming Soon RecyclerView */
        LinearLayoutManager comingSoonLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        mComingSoonRecyclerView = (RecyclerView) rootView.findViewById(R.id.coming_soon);
        mComingSoonRecyclerView.setLayoutManager(comingSoonLayoutManager);

        mMoviesComingSoonAdapter = new MoviesComingSoonAdapter(mMoviesComingSoon, this);

        loadMovies();

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
    public void onDestroy() {
        super.onDestroy();
    }

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

    private void loadMovies() {
        RestClient.getAuthenticated().getMovies(UserPreferences.getLatitude(), UserPreferences.getLongitude()).enqueue( new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {

                if (response.body() != null && response.isSuccessful()) {
                    mMoviesResponse = response.body();

                    mMoviesNewReleases.clear();
                    mMoviesTopBoxOffice.clear();
                    mMoviesComingSoon.clear();

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

                    if (mMoviesResponse != null) {
                        mMoviesNewReleases.addAll(mMoviesResponse.getNewReleases());
                        mNewReleasesRecyclerView.setAdapter(mMoviesNewReleasesAdapter);

                        mMoviesTopBoxOffice.addAll(mMoviesResponse.getTopBoxOffice());
                        mTopBoxOfficeRecyclerView.setAdapter(mMoviesTopBoxOfficeAdapter);

                        mMoviesComingSoon.addAll(mMoviesResponse.getComingSoon());
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
}
