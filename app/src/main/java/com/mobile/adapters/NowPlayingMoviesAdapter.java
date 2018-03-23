package com.mobile.adapters;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v13.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.mobile.Constants;
import com.mobile.MoviePosterClickListener;
import com.mobile.model.Movie;
import com.moviepass.R;

import java.util.ArrayList;

import io.realm.RealmList;

/**
 * Created by o_vicarra on 1/15/18.
 */

public class NowPlayingMoviesAdapter extends RecyclerView.Adapter<NowPlayingMoviesAdapter.ViewHolder> {
    private RealmList<Movie> moviesArrayList;
    private final MoviePosterClickListener moviePosterClickListener;
    private Context context;


    public NowPlayingMoviesAdapter(Context context, RealmList<Movie> moviesArrayList, MoviePosterClickListener moviePosterClickListener) {
        this.moviesArrayList = moviesArrayList;
        this.moviePosterClickListener = moviePosterClickListener;
        this.context = context;
    }


    @Override
    public NowPlayingMoviesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_movie_poster, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final NowPlayingMoviesAdapter.ViewHolder holder, int position) {
        final Movie nowPlaying = moviesArrayList.get(position);

        final Uri imgUrl = Uri.parse(nowPlaying.getImageUrl());
        holder.moviePoster.setImageURI(imgUrl);
        holder.movieTitle.setText("");
        holder.moviePoster.getHierarchy().setFadeDuration(500);

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                .setProgressiveRenderingEnabled(true)
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);
                        if (imgUrl.toString().contains("default")) {
                            holder.movieTitle.setText(nowPlaying.getTitle());
                        }
                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        holder.moviePoster.setImageResource(R.drawable.filmreel1);
                        holder.moviePoster.setImageURI(imgUrl + "/original.jpg");
                    }
                })
                .build();

        holder.moviePoster.setController(controller);

        ViewCompat.setTransitionName(holder.moviePoster, nowPlaying.getImageUrl());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moviePosterClickListener.onMoviePosterClick(holder.getAdapterPosition(), nowPlaying, holder.moviePoster);
            }
        });

    }


    @Override
    public int getItemCount() {
        return moviesArrayList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView movieTitle;
        SimpleDraweeView moviePoster;


        public ViewHolder(View itemView) {
            super(itemView);
            movieTitle = itemView.findViewById(R.id.poster_movie_title);
            moviePoster = itemView.findViewById(R.id.ticket_top_red_dark);
        }
    }
}
