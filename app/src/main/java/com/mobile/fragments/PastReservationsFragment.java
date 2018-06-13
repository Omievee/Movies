package com.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mobile.Interfaces.historyPosterClickListener;
import com.mobile.adapters.HistoryAdapter;
import com.mobile.helpers.LogUtils;
import com.mobile.model.Movie;
import com.moviepass.R;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by omievee on 1/27/18.
 */

public class PastReservationsFragment extends MPFragment implements historyPosterClickListener {

    public static final String TAG = PastReservationsFragment.class.getSimpleName();


    View rootview;
    HistoryAdapter historyAdapter;
    RecyclerView historyRecycler;
    RealmList<Movie> historyList;
    TextView noMovies;
    View progress;
    Activity myActivity;
    Context myContext;

    public PastReservationsFragment() {
    }


    public static PastReservationsFragment newInstance() {

        Bundle args = new Bundle();

        PastReservationsFragment fragment = new PastReservationsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootview = inflater.inflate(R.layout.fr_history, container, false);
        historyRecycler = rootview.findViewById(R.id.historyReycler);
        historyList = new RealmList<>();
        noMovies = rootview.findViewById(R.id.NoMoives);
        progress = rootview.findViewById(R.id.progress);
        LogUtils.newLog(TAG, "onCreateView: ");
        return rootview;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int numOfColumns = calculateNoOfColumns(myActivity);



        GridLayoutManager manager = new GridLayoutManager(myActivity, numOfColumns, GridLayoutManager.VERTICAL, false);
        historyRecycler.setLayoutManager(manager);
        historyAdapter = new HistoryAdapter(getActivity(), historyList, this);
        historyRecycler.setAdapter(historyAdapter);

        progress.setVisibility(View.VISIBLE);
        queryRealmForObjects();
    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 120);
        return noOfColumns;
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


    public void queryRealmForObjects() {
        historyList.clear();
        progress.setVisibility(View.GONE);

        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("History.Realm")
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm historyRealm = Realm.getInstance(config);

        RealmResults<Movie> allHIstory = historyRealm.where(Movie.class)
                .findAll();

        historyList.addAll(allHIstory);
        if (historyList.size() == 0) {
            historyRecycler.setVisibility(View.GONE);
            noMovies.setVisibility(View.VISIBLE);
        } else {
            historyRecycler.setVisibility(View.VISIBLE);
            noMovies.setVisibility(View.GONE);
        }
        if (historyAdapter != null) {
            historyRecycler.getRecycledViewPool().clear();
            historyAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPosterClicked(int pos, Movie historyposter, SimpleDraweeView sharedView) {
        showFragment(HistoryDetailsFragment.Companion.newInstance(historyposter, ViewCompat.getTransitionName(sharedView)));
    }
}



