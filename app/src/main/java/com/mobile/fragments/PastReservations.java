package com.mobile.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.mobile.adapters.HistoryAdapter;
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

public class PastReservations extends Fragment {

    View rootview;
    HistoryAdapter historyAdapter;
    RecyclerView historyRecycler;
    ArrayList<Movie> historyList;
    GridView historyGrid;
    TextView noMovies;
    View progress;
    HistoryResponse historyResponse;

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
        int numOfColumns = calculateNoOfColumns(getActivity());

        GridLayoutManager manager = new GridLayoutManager(getActivity(), numOfColumns, GridLayoutManager.VERTICAL, false);
        historyRecycler.setLayoutManager(manager);
        historyAdapter = new HistoryAdapter(getActivity(), historyList);
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


}
