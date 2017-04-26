package com.moviepass.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.activities.MovieActivity;
import com.moviepass.adapters.MoviePosterAdapter;
import com.moviepass.adapters.MoviesNewReleasesAdapter;
import com.moviepass.model.Movie;
import com.moviepass.model.MoviesResponse;
import com.moviepass.network.RestClient;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ryan on 4/25/17.
 */

public class MoviesFragment extends Fragment {

    private static final String MOVIES = "movies";

    private MoviesNewReleasesAdapter mMoviesNewReleasesAdapter;
    private MoviePosterAdapter mMoviePosterAdapter;

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

        mMoviesNewReleasesAdapter = new MoviesNewReleasesAdapter(getActivity(), onClickListener, mMoviesNewReleases);


        /* New Releases RecyclerView */
        LinearLayoutManager topBoxOfficeLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        mTopBoxOfficeRecyclerView = (RecyclerView) rootView.findViewById(R.id.top_box_office);
        mTopBoxOfficeRecyclerView.setLayoutManager(topBoxOfficeLayoutManager);

        mMoviePosterAdapter = new MoviePosterAdapter(getActivity(), onClickListener, mMoviesTopBoxOffice);

        /* New Releases RecyclerView */
        LinearLayoutManager comingSoonLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        mComingSoonRecyclerView = (RecyclerView) rootView.findViewById(R.id.coming_soon);
        mComingSoonRecyclerView.setLayoutManager(comingSoonLayoutManager);

        mMoviePosterAdapter = new MoviePosterAdapter(getActivity(), onClickListener, mMoviesComingSoon);

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
            mNewReleasesRecyclerView.setAdapter(mMoviePosterAdapter);
            mComingSoonRecyclerView.setAdapter(mMoviePosterAdapter);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return false;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Movie movie = mMovieArrayList.get((int) view.getTag());
            Intent moveIntent = new Intent(getActivity(), MovieActivity.class);
            moveIntent.putExtra(MovieActivity.MOVIE, Parcels.wrap(movie));

            startActivity(moveIntent);
        }
    };

    private void loadMovies() {
        RestClient.getAuthenticated().getMovies(UserPreferences.getLatitude(), UserPreferences.getLongitude()).enqueue( new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {

                if (response.body() != null) {
                    mMoviesResponse = response.body();

                    mMoviesNewReleases.clear();
                    if (mMoviesNewReleasesAdapter != null) {
                        mNewReleasesRecyclerView.getRecycledViewPool().clear();
                        mMoviePosterAdapter.notifyDataSetChanged();
                    }

                    if (mMoviesResponse != null) {
                        mMoviesNewReleases.addAll(mMoviesResponse.getNewReleases());
                        mNewReleasesRecyclerView.setAdapter(mMoviePosterAdapter);
                        
                        mMoviesTopBoxOffice.addAll(mMoviesResponse.getTopBoxOffice());
                        mTopBoxOfficeRecyclerView.setAdapter(mMoviePosterAdapter);

                        mMoviesComingSoon.addAll(mMoviesResponse.getComingSoon());
                        mComingSoonRecyclerView.setAdapter(mMoviePosterAdapter);

                    }



                }
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {

            }
        });
    }
}
