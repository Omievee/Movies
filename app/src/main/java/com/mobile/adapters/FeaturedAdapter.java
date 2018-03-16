package com.mobile.adapters;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v13.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.mobile.Constants;
import com.mobile.MoviePosterClickListener;
import com.mobile.model.Movie;
import com.moviepass.R;

import java.util.ArrayList;

import io.realm.RealmList;

/**
 * Created by o_vicarra on 1/17/18.
 */

public class FeaturedAdapter extends RecyclerView.Adapter<FeaturedAdapter.ViewHolder> {

    private final MoviePosterClickListener moviePosterClickListener;
    private RealmList<Movie> featuredMovie;
    private Context context;

    public FeaturedAdapter(Context context, RealmList<Movie> featuredMovie, MoviePosterClickListener moviePosterClickListener) {
        this.moviePosterClickListener = moviePosterClickListener;
        this.featuredMovie = featuredMovie;
        this.context = context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_featured_poster, parent, false);
        return new FeaturedAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final Movie movie = featuredMovie.get(position);
        final Uri imgURI = Uri.parse(movie.getLandscapeImageUrl());

        holder.moviePoster.setMinimumHeight(9 * holder.itemView.getContext().getResources().getDisplayMetrics().heightPixels / 16);

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(imgURI)
                .setProgressiveRenderingEnabled(true)
                .setSource(imgURI)
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);
                        if (imgURI.toString().contains("default")) {

                        }
                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        holder.moviePoster.setImageResource(R.drawable.filmreel1);
                    }
                })
                .build();

        holder.moviePoster.setController(controller);

        ViewCompat.setTransitionName(holder.moviePoster, movie.getImageUrl());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moviePosterClickListener.onMoviePosterClick(holder.getAdapterPosition(), movie, holder.moviePoster);
            }
        });

    }

    @Override
    public int getItemCount() {
        return featuredMovie.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView moviePoster;


        public ViewHolder(View itemView) {
            super(itemView);
            moviePoster = itemView.findViewById(R.id.featuredPoster);
        }
    }
}
