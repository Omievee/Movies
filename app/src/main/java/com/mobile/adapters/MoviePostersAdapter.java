package com.mobile.adapters;

import android.content.res.Resources;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v13.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmList;

/**
 * Created by ryan on 4/26/17.
 */

public class MoviePostersAdapter extends RecyclerView.Adapter<MoviePostersAdapter.ViewHolder> {

    // Interface

    private int dynamicListPosition;

    //Movie List
    private RealmList<Movie> moviesArrayList;

    MoviePosterClickListener listener;

    private final int TYPE_ITEM = 0;

    public MoviePostersAdapter(RealmList<Movie> moviesArrayList, MoviePosterClickListener listener, int dynamicListPosition) {
        this.listener = listener;
        this.moviesArrayList = moviesArrayList;
        this.dynamicListPosition = dynamicListPosition;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_item_movie_poster)
        RelativeLayout listItemMoviePoster;
        @BindView(R.id.poster_movie_title)
        TextView title;
        @BindView(R.id.ticket_top_red_dark)
        SimpleDraweeView mNewReleasePosterDV;
        @BindView(R.id.RELEASEDATE)
        TextView comingSoon;
        @BindView(R.id.frame)
        FrameLayout frame;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            listItemMoviePoster = v.findViewById(R.id.list_item_movie_poster);
            title = v.findViewById(R.id.poster_movie_title);
            mNewReleasePosterDV = v.findViewById(R.id.ticket_top_red_dark);
            comingSoon = v.findViewById(R.id.RELEASEDATE);
            frame = v.findViewById(R.id.frame);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_movie_poster, parent, false);
        return new ViewHolder(view);
    }

    public static int safeLongToInt(long l) {
        return (int) Math.max(Math.min(Integer.MAX_VALUE, l), Integer.MIN_VALUE);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        int aspectW = 2;
        int aspectH = 3;

        double width = holder.comingSoon.getContext().getResources().getDisplayMetrics().widthPixels;
        double finalWidth = width/2.5;
        double height = finalWidth * aspectH / aspectW;

       int w = safeLongToInt(Math.round(finalWidth));
       int h = safeLongToInt(Math.round(height));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w,h);
        holder.frame.setLayoutParams(params);


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
//            if(movie.getReleaseDate()==null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onMoviePosterClick(movie, holder.mNewReleasePosterDV);
                    }
                });
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