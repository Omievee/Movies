package com.mobile.adapters;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.mobile.Constants;
import com.mobile.MoviePosterClickListener;
import com.mobile.model.Movie;
import com.moviepass.R;

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
        holder.moviePoster.setMinimumHeight(9 * holder.itemView.getContext().getResources().getDisplayMetrics().heightPixels / 16);

        Uri imgURI = null;
        Uri videoURI = null;

        if (movie != null) {
            imgURI = Uri.parse(movie.getLandscapeImageUrl());
            videoURI = Uri.parse(movie.getTeaserVideoUrl());
        }

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(imgURI)
                .setProgressiveRenderingEnabled(true)
                .setSource(imgURI)
                .build();

        Uri finalImgURI = imgURI;
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);
                        if (finalImgURI.toString().contains("default")) {

                        }
                        Log.d(Constants.TAG, "onFinalImageSet: ");
                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        holder.moviePoster.setImageResource(R.drawable.filmreel1);
                        Log.d(Constants.TAG, "onFailure: ");
                    }
                })
                .build();

        holder.moviePoster.setController(controller);
        holder.featuredVideo.setControllerHideOnTouch(false);

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

        DefaultBandwidthMeter meter = new DefaultBandwidthMeter();
        DataSource.Factory data = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "moviepass"), meter);
        MediaSource video = new ExtractorMediaSource.Factory(data).createMediaSource(videoURI);

        player.prepare(video);
        holder.moviePoster.setVisibility(View.GONE);
        holder.featuredVideo.setPlayer(player);
        player.setRepeatMode(Player.REPEAT_MODE_ONE);

        player.setPlayWhenReady(true);

        fadeOut(holder.moviePoster);
        holder.moviePoster.setVisibility(View.GONE);
        fadeIn(holder.featuredVideo);
        holder.featuredVideo.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> moviePosterClickListener.onMoviePosterClick(holder.getAdapterPosition(), movie, holder.moviePoster));


        ImagePipeline pipeline = Fresco.getImagePipeline();
        pipeline.clearMemoryCaches();
        pipeline.clearDiskCaches();
    }

    @Override
    public int getItemCount() {
        return featuredMovie.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView moviePoster;
        PlayerView featuredVideo;

        public ViewHolder(View itemView) {
            super(itemView);
            featuredVideo = itemView.findViewById(R.id.featuredVideo);
            moviePoster = itemView.findViewById(R.id.featuredPoster);
        }
    }


    public void fadeIn(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(500);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        view.setAnimation(animation);

    }

    public void fadeOut(View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new DecelerateInterpolator()); //add this
        fadeOut.setDuration(500);
        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeOut);
        view.setAnimation(animation);
    }

}
