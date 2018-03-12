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

import com.helpshift.All;
import com.helpshift.support.contracts.SearchResultListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.mobile.Constants;
import com.mobile.Interfaces.AfterSearchListener;
import com.mobile.UserPreferences;
import com.mobile.adapters.SearchAdapter;
import com.mobile.helpers.ContextSingleton;
import com.mobile.helpers.GoWatchItSingleton;
import com.mobile.model.Movie;
import com.mobile.model.MoviesResponse;
import com.mobile.network.RestCallback;
import com.mobile.network.RestClient;
import com.mobile.network.RestError;
import com.mobile.responses.GoWatchItResponse;
import com.moviepass.BuildConfig;
import com.moviepass.R;

import org.parceler.Parcels;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public SearchFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fr_searchview, container, false);
        searchBar = rootView.findViewById(R.id.searchBar);
        progress = rootView.findViewById(R.id.progress);

        ALLMOVIES = new ArrayList<>();
        noDuplicates = new ArrayList<>();
        url = "http://moviepass.com/go/movies";
        if(GoWatchItSingleton.getInstance().getCampaign()!=null && GoWatchItSingleton.getInstance().getCampaign().equalsIgnoreCase("no_campaign"))
            url = url+"/"+GoWatchItSingleton.getInstance().getCampaign();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progress.setVisibility(View.VISIBLE);
        loadResults();
        LayoutInflater myInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        customAdapter = new SearchAdapter(myInflater,this);
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

//    public void searchEvent(String search){
//
//        String l = String.valueOf(UserPreferences.getLatitude());
//        String ln = String.valueOf(UserPreferences.getLongitude());
//        String userId = String.valueOf(UserPreferences.getUserId());
//        String deep_link="";
//
//        String versionName = BuildConfig.VERSION_NAME;
//        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
//        String campaign = GoWatchItSingleton.getInstance().getCampaign();
//
//
//        RestClient.getAuthenticatedAPIGoWatchIt().searchTheatersMovies("search","true",
//                "Movie","-1",search,campaign,"app","android",deep_link,"organic",
//                l,ln,userId,"IDFA", versionCode, versionName).enqueue(new RestCallback<GoWatchItResponse>() {
//            @Override
//            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
//                GoWatchItResponse responseBody = response.body();
////                progress.setVisibility(View.GONE);
//
//                Log.d("HEADER SEARCH -- >", "onResponse: "+responseBody.getFollowUrl());
//            }
//
//            @Override
//            public void failure(RestError restError) {
////                progress.setVisibility(View.GONE);
//                // Toast.makeText(MovieActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
//    }

    @Override
    public void getSearchString() {
        if(searchBar!=null && searchBar.getText()!=null)
            GoWatchItSingleton.getInstance().searchEvent(searchBar.getText(),"search",url);
    }
}
