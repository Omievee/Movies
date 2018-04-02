package com.mobile.adapters;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.facebook.imagepipeline.core.ImagePipeline;
import com.helpshift.support.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.mobile.MoviePosterClickListener;
import com.mobile.model.Movie;
import com.moviepass.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmList;

/**
 * Created by ryan on 4/26/17.
 */

public class MoviesTopBoxOfficeAdapter extends RecyclerView.Adapter<MoviesTopBoxOfficeAdapter.ViewHolder> {
    public static final String TAG = "found it...";
    private final MoviePosterClickListener moviePosterClickListener;
    private RealmList<Movie> moviesArrayList;

    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;

    public MoviesTopBoxOfficeAdapter(Context context, RealmList<Movie> moviesArrayList, MoviePosterClickListener moviePosterClickListener) {
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
        SimpleDraweeView mTopBoxMovieDV;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            listItemMoviePoster = v.findViewById(R.id.list_item_movie_poster);
            title = v.findViewById(R.id.poster_movie_title);
            mTopBoxMovieDV = v.findViewById(R.id.ticket_top_red_dark);
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
        holder.title.setText("");
        final Uri imgUrl = Uri.parse(movie.getImageUrl());
        holder.mTopBoxMovieDV.setImageURI(imgUrl);
        holder.mTopBoxMovieDV.getHierarchy().setFadeDuration(500);
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
                            holder.title.setText(movie.getTitle());
                        }


                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        holder.mTopBoxMovieDV.setImageURI(imgUrl + "/original.jpg");
                    }
                })

                .build();
        holder.mTopBoxMovieDV.setController(controller);


        android.support.v4.view.ViewCompat.setTransitionName(holder.mTopBoxMovieDV, movie.getImageUrl());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moviePosterClickListener.onMoviePosterClick(holder.getAdapterPosition(), movie, holder.mTopBoxMovieDV);
            }
        });


        ImagePipeline pipeline = Fresco.getImagePipeline();
        pipeline.clearMemoryCaches();
        pipeline.clearDiskCaches();
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
