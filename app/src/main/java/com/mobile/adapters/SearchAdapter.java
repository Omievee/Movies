package com.mobile.adapters;

import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import com.mobile.search.AfterSearchListener;
import com.mobile.model.Movie;
import com.moviepass.R;

import java.util.List;


/**
 * Created by o_vicarra on 1/29/18.
 */

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SuggestionHolder>{

    View root;
    AfterSearchListener listener;
    private List<Movie> moviesArrayList;

    public SearchAdapter(AfterSearchListener listener, List<Movie> moviesArrayList) {
        this.listener = listener;
        this.moviesArrayList = moviesArrayList;
    }

    @Override
    public SuggestionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_moviesearch, parent, false);
        return new SuggestionHolder(view);
    }

    @Override
    public void onBindViewHolder(SuggestionHolder holder, int position) {
        Movie suggestion = moviesArrayList.get(holder.getAdapterPosition());
        holder.title.setText(suggestion.getTitle());
        holder.rating.setText("Rated " + suggestion.getRating());
        int t = suggestion.getRunningTime();
        int hours = t / 60; //since both are ints, you get an int
        int minutes = t % 60;
        if (t == 0) {
            holder.runTime.setVisibility(View.GONE);
        } else if (hours > 1) {
            String translatedRunTime = hours + " hours " + minutes + " minutes";
            holder.runTime.setText(translatedRunTime);
        } else {
            String translatedRunTime = hours + " hour " + minutes + " minutes";
            holder.runTime.setText(translatedRunTime);
        }

        final Uri imgUrl = Uri.parse(suggestion.getImageUrl());
        holder.image.setImageURI(imgUrl);
        holder.image.getHierarchy().setFadeDuration(500);

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                .setProgressiveRenderingEnabled(true)
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);

                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        holder.image.setImageURI(imgUrl + "/original.jpg");
                    }
                })
                .build();

        holder.image.setController(controller);

        holder.cardView.setOnClickListener(view -> {
            listener.getSearchString(suggestion);
        });

    }

    public void updateList(List<Movie> list){
        moviesArrayList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return moviesArrayList.size();
    }

    public class SuggestionHolder extends RecyclerView.ViewHolder {
        protected TextView title;
        protected TextView rating;
        protected TextView runTime;
        protected SimpleDraweeView image;
        View cardView;

        public SuggestionHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.MovieSearch_Title);
            rating = itemView.findViewById(R.id.MovieSearch_Rating);
            runTime = itemView.findViewById(R.id.MovieSearch_RunTIme);
            image = itemView.findViewById(R.id.MovieSearch_Poster);
            cardView = itemView.findViewById(R.id.cardMovie);
        }
    }

}
