package com.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.mobile.Interfaces.AfterSearchListener;
import com.mobile.adapters.SearchAdapter;
import com.mobile.helpers.GoWatchItSingleton;
import com.mobile.model.Movie;
import com.moviepass.R;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by o_vicarra on 2/6/18.
 */

public class SearchFragment extends Fragment implements AfterSearchListener {
    public EditText searchBar;
    View rootView;
    SearchAdapter customAdapter;
    RealmList<Movie> ALLMOVIES;
    View progress;
    ArrayList<Movie> noDuplicates;
    String url;
    private RealmResults<Movie> movies;
    private RealmResults<Movie> allMovies;
    private RecyclerView recyclerView;
    private RealmList<Movie> suggestions;
    private ImageView backArrow, removeIcon;

    public SearchFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fr_searchview, container, false);
        searchBar = rootView.findViewById(R.id.searchBar);
        progress = rootView.findViewById(R.id.progress);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        backArrow = rootView.findViewById(R.id.backArrow);
        removeIcon = rootView.findViewById(R.id.removeIcon);

        ALLMOVIES = new RealmList<>();

        noDuplicates = new ArrayList<>();
        url = "http://moviepass.com/go/movies";
        if (GoWatchItSingleton.getInstance().getCampaign() != null && !GoWatchItSingleton.getInstance().getCampaign().equalsIgnoreCase("no_campaign"))
            url = url + "/" + GoWatchItSingleton.getInstance().getCampaign();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        progress.setVisibility(View.VISIBLE);
        LayoutInflater myInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        searchBar.requestFocus();


//        getMovies();
        getAllMovies();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String movieSearch = charSequence.toString();
                boolean isMovieDuplicated = false;
                if (movieSearch.equals("")) {
                    customAdapter.updateList(ALLMOVIES);
                } else {
                    suggestions = new RealmList<>();
                    for (Movie movieTitle : ALLMOVIES) {
                        if (movieTitle.getTitle().toLowerCase().contains(movieSearch.toLowerCase())) {
                            for (Movie movieDuplicate : suggestions) {
                                if (movieDuplicate.getId() == movieTitle.getId()) {
                                    isMovieDuplicated = true;
                                }

                            }
                            if (isMovieDuplicated == false)
                                suggestions.add(movieTitle);
                        }
                    }
                    customAdapter.updateList(suggestions);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(getActivity());
                getActivity().onBackPressed();
            }
        });

        removeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!searchBar.getText().toString().trim().isEmpty()){
                    customAdapter.updateList(ALLMOVIES);
                    searchBar.setText("");
                }
            }
        });

    }

    public void getMovies(){
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("Movies.Realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm moviesRealm = Realm.getInstance(config);
        movies = moviesRealm.where(Movie.class)
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


    }

    public void getAllMovies(){
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("AllMovies.Realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm moviesRealm = Realm.getInstance(config);
        allMovies = moviesRealm.where(Movie.class).findAll();
//
//        HashMap<Integer, Movie> movieHashMap = new HashMap<>();
//        for (Movie movie : movies) {
//            movieHashMap.put(movie.getId(), movie);
//        }
        ALLMOVIES.clear();
        for (Movie movie : allMovies) {
            ALLMOVIES.add(movie);
        }

//        for (Movie movie : movieHashMap.values()) {
//            ALLMOVIES.add(movie);
//        }
//        LogUtils.newLog(TAG, "getAllMovies: ALL MOVIES "+ALLMOVIES.size());
        customAdapter = new SearchAdapter(this,ALLMOVIES);
        recyclerView.setAdapter(customAdapter);
        progress.setVisibility(View.GONE);
        showSfotKeyboard();
    }

    @Override
    public void getSearchString(Movie movie) {
        String url = "https://moviepass.com/go/movies";
        GoWatchItSingleton.getInstance().searchEvent(searchBar.getText().toString(), "search", url);
        MoviesFragment moviesFragment = (MoviesFragment) getParentFragment();
        MovieFragment movieFragment = MovieFragment.newInstance(movie,"");
        moviesFragment.showFragment(movieFragment);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void showSfotKeyboard(){
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(getActivity().getCurrentFocus(), InputMethodManager.SHOW_IMPLICIT);
    }


    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

}