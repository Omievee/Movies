package com.moviepass.adapters;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.moviepass.MoviePosterClickListener;
import com.moviepass.R;
import com.moviepass.model.Movie;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ryan on 4/26/17.
 */

public class MoviesNewReleasesAdapter extends RecyclerView.Adapter<MoviesNewReleasesAdapter.ViewHolder> {
    public static final String TAG = "found it...";
    private final MoviePosterClickListener moviePosterClickListener;
    private ArrayList<Movie> moviesArrayList;

    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;

    public MoviesNewReleasesAdapter(Context context, ArrayList<Movie> moviesArrayList, MoviePosterClickListener moviePosterClickListener) {
        this.moviePosterClickListener = moviePosterClickListener;
        this.moviesArrayList = moviesArrayList;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_item_movie_poster)
        RelativeLayout listItemMoviePoster;
        @BindView(R.id.poster_movie_title)
        TextView title;
        @BindView(R.id.ticket_top_red_dark)
        SimpleDraweeView mNewReleasePosterDV;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            listItemMoviePoster = v.findViewById(R.id.list_item_movie_poster);
            title = v.findViewById(R.id.poster_movie_title);
            mNewReleasePosterDV = v.findViewById(R.id.ticket_top_red_dark);

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_movie_poster, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Movie movie = moviesArrayList.get(position);
        final Uri imgUrl = Uri.parse(movie.getImageUrl());
        holder.mNewReleasePosterDV.setImageURI(imgUrl);
        holder.title.setText("");
        holder.mNewReleasePosterDV.getHierarchy().setFadeDuration(500);

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                .setProgressiveRenderingEnabled(true)
                .setSource(imgUrl)
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);
                        if (imgUrl.toString().contains("updateMovieThumb")) {
                            holder.mNewReleasePosterDV.setImageResource(R.drawable.filmreel1);
                            holder.mNewReleasePosterDV.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE);
                            holder.mNewReleasePosterDV.setBackgroundColor(holder.itemView.getResources().getColor(R.color.test_blue));
                            holder.title.setText(movie.getTitle());
                        }
                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        holder.title.setText(movie.getTitle());
                        holder.title.setGravity(View.TEXT_ALIGNMENT_GRAVITY);
                    }
                })
                .build();


        holder.mNewReleasePosterDV.setController(controller);


        ViewCompat.setTransitionName(holder.mNewReleasePosterDV, movie.getImageUrl());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moviePosterClickListener.onMoviePosterClick(holder.getAdapterPosition(), movie, holder.mNewReleasePosterDV);
            }
        });
    }

    @Override
    public int getItemCount() {
        return moviesArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }


}