package com.moviepass.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moviepass.R;
import com.moviepass.adapters.MoviePassCardAdapter;
import com.moviepass.model.MoviePassCard;
import com.moviepass.network.RestClient;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 8/1/17.
 */

public class ProfileMoviePassCardFragment extends Fragment {

    ArrayList<MoviePassCard> moviePassCardArrayList;
    MoviePassCardAdapter moviePassCardAdapter;

    @BindView(R.id.recycler_view)
    RecyclerView moviepassCardRecyclerView;
    @BindView(R.id.text_no_card)
    TextView textNoCard;
    @BindView(R.id.progress)
    View progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile_moviepass_card, container, false);
        ButterKnife.bind(this, rootView);

        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("MoviePass Card");

        moviePassCardArrayList = new ArrayList<>();

        LinearLayoutManager mLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        moviepassCardRecyclerView = rootView.findViewById(R.id.recycler_view);
        moviepassCardRecyclerView.setLayoutManager(mLayoutManager);

        progress = rootView.findViewById(R.id.progress);
        textNoCard = rootView.findViewById(R.id.text_no_card);

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(250);
        itemAnimator.setRemoveDuration(250);
        moviepassCardRecyclerView.setItemAnimator(itemAnimator);

        moviePassCardAdapter = new MoviePassCardAdapter(moviePassCardArrayList);

        loadMoviePassCards();

        return rootView;
    }

    private void loadMoviePassCards() {
        progress.setVisibility(View.VISIBLE);

        RestClient.getAuthenticated().getMoviePassCards().enqueue(new Callback<List<MoviePassCard>>() {
            @Override
            public void onResponse(Call<List<MoviePassCard>> call, Response<List<MoviePassCard>> response) {
                progress.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<MoviePassCard> moviePassCardsResponse = response.body();

                    moviePassCardArrayList.clear();

                    if (moviePassCardAdapter != null) {
                        moviepassCardRecyclerView.getRecycledViewPool().clear();
                        moviePassCardAdapter.notifyDataSetChanged();
                    }

                    moviePassCardArrayList.addAll(moviePassCardsResponse);

                    Log.d("resultList", "resultList: " + moviePassCardsResponse);

                    if (moviePassCardArrayList != null && moviePassCardArrayList.size() == 0) {
                        moviepassCardRecyclerView.setVisibility(View.GONE);
                        textNoCard.setVisibility(View.VISIBLE);
                    } else {
                        moviepassCardRecyclerView.setVisibility(View.VISIBLE);
                        textNoCard.setVisibility(View.GONE);

                        moviepassCardRecyclerView.setAdapter(moviePassCardAdapter);
                        moviepassCardRecyclerView.setTranslationY(0);
                        moviepassCardRecyclerView.setAlpha(1.0f);
                    }

                }

            }

            @Override
            public void onFailure(Call<List<MoviePassCard>> call, Throwable t) {
                progress.setVisibility(View.GONE);
            }
        });
    }

    void manageVisiblity() {
        if (moviePassCardArrayList != null && moviePassCardArrayList.size() == 0) {
            moviepassCardRecyclerView.setVisibility(View.GONE);
            textNoCard.setVisibility(View.VISIBLE);
        } else {
            moviepassCardRecyclerView.setVisibility(View.VISIBLE);
            textNoCard.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}

