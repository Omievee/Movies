package com.mobile.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mobile.Interfaces.AfterSearchListener;
import com.mobile.UserPreferences;
import com.mobile.adapters.SearchAdapter;
import com.mobile.helpers.GoWatchItSingleton;
import com.mobile.model.Movie;
import com.mobile.model.MoviesResponse;
import com.mobile.network.RestClient;
import com.mobile.responses.LocalStorageMovies;
import com.moviepass.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.facebook.GraphRequest.TAG;

/**
 * Created by o_vicarra on 2/6/18.
 */

public class SearchFragment extends Fragment implements AfterSearchListener {
    MaterialSearchBar searchBar;
    View rootView;
    SearchAdapter customAdapter;
    ArrayList<Movie> ALLMOVIES;
    View progress;
    ArrayList<Movie> noDuplicates;
    String url;
    Activity myActivity;
    Context myContext;


    public SearchFragment() {
    }


//    public static SearchFragment newInstance(Movie movie) {
//        SearchFragment fragment = new SearchFragment();
//        Bundle args = new Bundle();
//        args.putParcelable("NR", movie);
//        args.putParcelable("FE", movie);
//        args.putParcelable("NP", movie);
//        args.putParcelable("TB", movie);
//
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fr_searchview, container, false);
        searchBar = rootView.findViewById(R.id.searchBar);
        progress = rootView.findViewById(R.id.progress);


        ALLMOVIES = new ArrayList<>();


        noDuplicates = new ArrayList<>();
        url = "http://moviepass.com/go/movies";
        if (GoWatchItSingleton.getInstance().getCampaign() != null && !GoWatchItSingleton.getInstance().getCampaign().equalsIgnoreCase("no_campaign"))
            url = url + "/" + GoWatchItSingleton.getInstance().getCampaign();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        searchBar.enableSearch();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progress.setVisibility(View.VISIBLE);


        loadResults();

        LayoutInflater myInflater = (LayoutInflater) myActivity.getSystemService(LAYOUT_INFLATER_SERVICE);
        customAdapter = new SearchAdapter(myInflater, this);
        customAdapter.setSuggestions(ALLMOVIES);
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

    public void loadResults() {
        Log.d(TAG, "loadResults: ");
        RestClient.getAuthenticated().getMovies(UserPreferences.getLatitude(), UserPreferences.getLongitude()).enqueue(new Callback<MoviesResponse>() {

            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                MoviesResponse info = response.body();
                if (response.isSuccessful() && response != null) {
                    progress.setVisibility(View.GONE);
                    ALLMOVIES.clear();
                    ALLMOVIES.addAll(info.getFeatured());
                    ALLMOVIES.addAll(info.getNewReleases());
                    ALLMOVIES.addAll(info.getNowPlaying());
                    ALLMOVIES.addAll(info.getTopBoxOffice());
                    HashMap<Integer, Movie> movieHashMap = new HashMap<>();
                    for (Movie movie : ALLMOVIES) {
                        movieHashMap.put(movie.getId(), movie);
                    }
                    ALLMOVIES.clear();
                    for (Movie movie : movieHashMap.values()) {
                        ALLMOVIES.add(movie);
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

    @Override
    public void getSearchString() {
        String url = "https://moviepass.com/go/movies";
        GoWatchItSingleton.getInstance().searchEvent(searchBar.getText().toString(), "search", url);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myContext = context;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        myActivity = activity;
    }
}
