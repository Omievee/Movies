package com.mobile.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.adapters.SearchAdapter;
import com.mobile.model.Movie;
import com.mobile.model.MoviesResponse;
import com.mobile.network.RestClient;
import com.moviepass.R;

import org.parceler.Parcels;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by o_vicarra on 2/6/18.
 */

public class SearchFragment extends Fragment {

    public static final String MOVIES = "movies";
    MaterialSearchBar searchBar;
    View rootView;
    SearchAdapter customAdapter;
    ArrayList<Movie> ALLMOVIES;
    MoviesFragment moviesFrag;
    View progress;
    public SearchFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fr_searchview, container, false);
        searchBar = rootView.findViewById(R.id.searchBar);

        progress = rootView.findViewById(R.id.progress);
        ALLMOVIES = new ArrayList<>();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progress.setVisibility(View.VISIBLE);
        loadResults();
        LayoutInflater myInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        customAdapter = new SearchAdapter(myInflater);
        Handler handler = new Handler();
        customAdapter.setSuggestions(ALLMOVIES);
        handler.postDelayed(() -> customAdapter.setSuggestions(ALLMOVIES), 500);
        searchBar.setCustomSuggestionAdapter(customAdapter);
        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                customAdapter.getFilter().filter(searchBar.getText());

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

    //
    public void loadResults() {
        RestClient.getAuthenticated().getMovies(UserPreferences.getLatitude(), UserPreferences.getLongitude()).enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                MoviesResponse info = response.body();
                if (response.isSuccessful() && response != null) {
                    progress.setVisibility(View.GONE);
                    ALLMOVIES.addAll(info.getFeatured());
                    ALLMOVIES.addAll(info.getNowPlaying());
                    ALLMOVIES.addAll(info.getTopBoxOffice());
                    ALLMOVIES.addAll(info.getNewReleases());

                    //Filter out duplicates
                    Log.d(Constants.TAG, "size first: " + ALLMOVIES.size());
                    for (int i = 0; i < ALLMOVIES.size(); i++) {
                        for (int j = 1; j < ALLMOVIES.size(); j++) {
                            if (ALLMOVIES.get(i).getId() == (ALLMOVIES.get(j).getId())) {
                                ALLMOVIES.remove(j);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Server Response Failed; Try again", Toast.LENGTH_SHORT).show();

            }
        });
    }



}
