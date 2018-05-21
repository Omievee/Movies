package com.mobile.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v13.view.ViewCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmList;

/**
 * Created by ryan on 4/26/17.
 */

public class MoviesNewReleasesAdapter extends RecyclerView.Adapter<MoviesNewReleasesAdapter.ViewHolder> {
    private final MoviePosterClickListener moviePosterClickListener;
    private RealmList<Movie> moviesArrayList;


    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;
    private GestureDetectorCompat mDetector;

    public MoviesNewReleasesAdapter(Context context, RealmList<Movie> moviesArrayList, MoviePosterClickListener moviePosterClickListener) {
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

        try {
            holder.mNewReleasePosterDV.setImageURI(imgUrl);
            holder.mNewReleasePosterDV.getHierarchy().setFadeDuration(500);

            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                    .setProgressiveRenderingEnabled(true)
                    .setSource(imgUrl)
                    .build();

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setOldController(holder.mNewReleasePosterDV.getController())
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
                            holder.mNewReleasePosterDV.setImageURI(imgUrl + "/original.jpg");

                        }
                    })
                    .build();

            if (imgUrl.toString().contains("default")) {
                holder.mNewReleasePosterDV.refreshDrawableState();
            }
            holder.mNewReleasePosterDV.setController(controller);


            ViewCompat.setTransitionName(holder.mNewReleasePosterDV, movie.getImageUrl());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moviePosterClickListener.onMoviePosterClick(holder.getAdapterPosition(), movie, holder.mNewReleasePosterDV);
                }
            });

//            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    moviePosterClickListener.onMoviePosterLongClick(holder.getAdapterPosition(), movie, holder.mNewReleasePosterDV);
//                    return true;
//                }
//            });
//
//            holder.itemView.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//
//                    } else if (event.getAction() == MotionEvent.ACTION_UP){
//                        moviePosterClickListener.releaseLongPress();
//                    }
//                    return false;
//                }
//
//
//            });


        }catch (IllegalStateException onBind) {
            onBind.printStackTrace();

        }
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