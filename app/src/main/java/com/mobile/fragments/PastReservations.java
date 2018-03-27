package com.mobile.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.mobile.Constants;
import com.mobile.Interfaces.historyPosterClickListener;
import com.mobile.adapters.HistoryAdapter;
import com.mobile.helpers.HistoryDetails;
import com.mobile.model.Movie;
import com.mobile.network.RestClient;
import com.mobile.responses.HistoryResponse;
import com.moviepass.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by omievee on 1/27/18.
 */

public class PastReservations extends Fragment implements historyPosterClickListener {

    public static final String TAG = PastReservations.class.getSimpleName();


    View rootview;
    HistoryAdapter historyAdapter;
    RecyclerView historyRecycler;
    ArrayList<Movie> historyList;
    TextView noMovies;
    View progress;
    HistoryResponse historyResponse;
    Activity myActivity;
    Context myContext;

    public PastReservations() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootview = inflater.inflate(R.layout.fr_history, container, false);
        historyRecycler = rootview.findViewById(R.id.historyReycler);
        historyList = new ArrayList<>();
        noMovies = rootview.findViewById(R.id.NoMoives);
        progress = rootview.findViewById(R.id.progress);

        return rootview;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int numOfColumns = calculateNoOfColumns(myActivity);

        GridLayoutManager manager = new GridLayoutManager(myActivity, numOfColumns, GridLayoutManager.VERTICAL, false);
        historyRecycler.setLayoutManager(manager);
        historyAdapter = new HistoryAdapter(myActivity, historyList, this);
        historyRecycler.setAdapter(historyAdapter);

        progress.setVisibility(View.VISIBLE);
        loadHIstory();
    }


    private void loadHIstory() {
        historyList.clear();
        RestClient.getAuthenticated().getReservations().enqueue(new Callback<HistoryResponse>() {
            @Override
            public void onResponse(Call<HistoryResponse> call, Response<HistoryResponse> response) {
                historyResponse = response.body();
                if (response != null && response.isSuccessful()) {
                    progress.setVisibility(View.GONE);
                    Log.d(Constants.TAG, "onResponse: " + historyResponse.getReservations());

                    if (historyResponse.getReservations().size() == 0) {
                        historyRecycler.setVisibility(View.GONE);
                        noMovies.setVisibility(View.VISIBLE);
                    } else {
                        historyList.addAll(historyResponse.getReservations());
                        historyRecycler.setVisibility(View.VISIBLE);
                        noMovies.setVisibility(View.GONE);
                    }
                    if (historyAdapter != null) {
                        historyRecycler.getRecycledViewPool().clear();
                        historyAdapter.notifyDataSetChanged();
                    }
                }

            }

            @Override
            public void onFailure(Call<HistoryResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Log.d(Constants.TAG, "onFailure: " + t.getMessage());

            }
        });
    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 110);
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


    @Override
    public void onPosterClicked(int pos, Movie historyposter, SimpleDraweeView sharedView) {
        HistoryDetailsFragment detailsFragment = HistoryDetailsFragment.newInstance(historyposter, ViewCompat.getTransitionName(sharedView));
        detailsFragment.setSharedElementEnterTransition(new HistoryDetails());
        detailsFragment.setEnterTransition(new Fade());
        detailsFragment.setExitTransition(new Fade());
        detailsFragment.setSharedElementReturnTransition(new HistoryDetails());

        FragmentManager fragmentManager = myActivity.getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.profile_container, detailsFragment);
        transaction.addToBackStack("");
        transaction.commit();

    }
}
