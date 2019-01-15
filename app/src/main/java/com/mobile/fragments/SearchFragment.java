package com.mobile.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.mobile.adapters.SearchAdapter;
import com.mobile.keyboard.KeyboardManager;
import com.mobile.model.Movie;
import com.mobile.movie.MoviesManager;
import com.mobile.search.AfterSearchListener;
import com.moviepass.R;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import io.reactivex.disposables.Disposable;

/**
 * Created by o_vicarra on 2/6/18.
 */

public class SearchFragment extends MPFragment implements AfterSearchListener {

    @Inject
    MoviesManager manager;

    @Inject
    KeyboardManager keyboardManager;
    public EditText searchBar;
    View rootView;
    SearchAdapter customAdapter;
    List<Movie> ALLMOVIES;
    View progress;
    ArrayList<Movie> noDuplicates;
    String url;
    private RecyclerView recyclerView;
    private List<Movie> suggestions;
    private ImageView backArrow, removeIcon;

    @Nullable
    Disposable movieSub;

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

        noDuplicates = new ArrayList<>();
        url = "http://moviepass.com/go/movies";
//        if (GoWatchItSingleton.getInstance().getCampaign() != null && !GoWatchItSingleton.getInstance().getCampaign().equalsIgnoreCase("no_campaign"))
//            url = url + "/" + GoWatchItSingleton.getInstance().getCampaign();

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
                search(movieSearch);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboardManager.hide();
                getActivity().onBackPressed();
            }
        });

        removeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchBar.getText().toString().trim().isEmpty()) {
                    customAdapter.updateList(ALLMOVIES);
                    searchBar.setText("");
                }
            }
        });

    }

    private void search(String movieSearch) {
        if (ALLMOVIES == null) {
            return;
        }
        boolean isMovieDuplicated = false;
        if (movieSearch.equals("")) {
            customAdapter.updateList(ALLMOVIES);
        } else {
            suggestions = new ArrayList<>();
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

    public void getAllMovies() {
        if (movieSub != null) {
            movieSub.dispose();
        }
        movieSub = manager
                .getAllMovies()
                .subscribe(v -> {
                    ALLMOVIES = v;
                    customAdapter = new SearchAdapter(this, ALLMOVIES);
                    recyclerView.setAdapter(customAdapter);
                    search(searchBar.getText().toString());
                }, d -> {
                    d.printStackTrace();
                });

        progress.setVisibility(View.GONE);
        showKeyboard();
    }

    @Override
    public void getSearchString(Movie movie) {
        showFragment(ScreeningsFragment.Companion.newInstance(new ScreeningsData(null, movie)));
    }

    public void showKeyboard() {
        keyboardManager.show();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AndroidSupportInjection.inject(this);
    }
}