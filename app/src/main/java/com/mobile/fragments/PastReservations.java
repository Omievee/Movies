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
//        historyGrid = rootview.findViewById(R.id.gridHistory);

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
        int noOfColumns = (int) (dpWidth / 120);
        return noOfColumns;
    }

//    private ArrayAdapter<Movie> buildAdapter() {
//        return new ArrayAdapter<Movie>(getActivity(), R.layout.list_item_history, historyList) {
//            @Override
//            public View getView(int position, View convertView, ViewGroup parent) {
//                ViewHolderMovie holder;
//
//                if (convertView == null) {
//                    convertView = View.inflate(getContext(), R.layout.fr_history, null);
//                    holder = new ViewHolderMovie(convertView);
//
//                    convertView.setTag(holder);
//                } else {
//                    holder = (ViewHolderMovie) convertView.getTag();
//                }
//
//                Movie movie = getItem(position);
//
//                final Movie m = historyList.get(position);
//                final Uri imgUrl = Uri.parse(movie.getImageUrl());
//                holder.imagePoster.setImageURI(imgUrl);
//                holder.imagePoster.getHierarchy().setFadeDuration(500);
//
//
//                Log.d(Constants.TAG, "onBindViewHolder: " + imgUrl);
//                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
//                        .setProgressiveRenderingEnabled(true)
//                        .setSource(imgUrl)
//                        .build();
//
//                DraweeController controller = Fresco.newDraweeControllerBuilder()
//                        .setImageRequest(request)
//                        .setOldController(holder.imagePoster.getController())
//                        .setControllerListener(new BaseControllerListener<ImageInfo>() {
//                            @Override
//                            public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
//                                super.onFinalImageSet(id, imageInfo, animatable);
//                                if (imgUrl.toString().contains("default")) {
//                                    holder.title.setText(movie.getTitle());
//                                }
//                            }
//
//                            @Override
//                            public void onFailure(String id, Throwable throwable) {
//                                holder.imagePoster.setImageURI(imgUrl + "/original.jpg");
//                            }
//                        })
//                        .build();
//
//                if (imgUrl.toString().contains("default")) {
//                    holder.imagePoster.refreshDrawableState();
//                }
//                holder.imagePoster.setController(controller);
//
//                holder.title.setText(movie.getTitle());
//
//                long createdAt = movie.getCreatedAt();
//                Date date = new Date(createdAt);
//                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
//                holder.date.setText(sdf.format(date));
//
//                return convertView;
//            }
//
//        };
//    }
//
//    static class ViewHolderMovie {
//        @BindView(R.id.historyPoster)
//        SimpleDraweeView imagePoster;
//        @BindView(R.id.Title_Movie)
//        TextView title;
//        @BindView(R.id.Date_seen)
//        TextView date;
//
//        public ViewHolderMovie(View v) {
//            ButterKnife.bind(this, v);
//        }
//    }

}
