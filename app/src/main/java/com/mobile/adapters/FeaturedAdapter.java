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
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.mobile.Constants;
import com.mobile.MoviePosterClickListener;
import com.mobile.helpers.LogUtils;
import com.mobile.model.Movie;
import com.moviepass.R;

import java.io.File;

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


        if (movie != null) {
            imgURI = Uri.parse(movie.getLandscapeImageUrl());
            Log.d(Constants.TAG, "Image URL >>>>>>>>>>>>>>>>> " + imgURI);
            if (movie.getTeaserVideoUrl() != null) {

                holder.featuredVideo.setControllerHideOnTouch(false);

                BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
                TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
                SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);


                player.addListener(new Player.EventListener() {
                    @Override
                    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

                    }

                    @Override
                    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

                    }

                    @Override
                    public void onLoadingChanged(boolean isLoading) {

                    }

                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        if (playbackState == Player.STATE_READY) {
                            fadeOut(holder.moviePoster);
                            holder.moviePoster.setVisibility(View.GONE);
                            fadeIn(holder.videoLayout);
                            holder.videoLayout.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override

                    public void onRepeatModeChanged(int repeatMode) {

                    }

                    @Override
                    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

                    }

                    @Override
                    public void onPlayerError(ExoPlaybackException error) {

                        fadeOut(holder.videoLayout);
                        holder.videoLayout.setVisibility(View.GONE);
                        fadeIn(holder.moviePoster);
                        holder.moviePoster.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onPositionDiscontinuity(int reason) {

                    }

                    @Override
                    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

                    }

                    @Override
                    public void onSeekProcessed() {

                    }
                });

                MediaSource video = new ExtractorMediaSource(Uri.parse(movie.getTeaserVideoUrl()), new CacheDataSourceFactory(context, 100 * 1024 * 1024, 5 * 1024 * 1024), new DefaultExtractorsFactory(), null, null);

                if (movie.getTeaserVideoUrl().matches(video.toString())) {
                    LogUtils.newLog(Constants.TAG, "matches: ");
                }


                player.prepare(video);
                player.setPlayWhenReady(true);
                player.setVolume(Player.DISCONTINUITY_REASON_INTERNAL);
                player.setRepeatMode(Player.REPEAT_MODE_ONE);

                holder.featuredVideo.setPlayer(player);
                holder.videoTitle.setText(movie.getTitle());
            }

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
                        LogUtils.newLog(Constants.TAG, "onFinalImageSet: ");
                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        holder.moviePoster.setImageResource(R.drawable.filmreel1);
                        LogUtils.newLog(Constants.TAG, "onFailure: ");
                    }
                })
                .build();

        holder.moviePoster.setController(controller);
        holder.itemView.setOnClickListener(v -> moviePosterClickListener.onMoviePosterClick(holder.getAdapterPosition(), movie, holder.moviePoster));

    }

    @Override
    public int getItemCount() {
        return featuredMovie.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView moviePoster;
        PlayerView featuredVideo;
        FrameLayout videoLayout;
        TextView videoTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            featuredVideo = itemView.findViewById(R.id.featuredVideo);
            moviePoster = itemView.findViewById(R.id.featuredPoster);
            videoLayout = itemView.findViewById(R.id.videoFrameLayout);
            videoTitle = itemView.findViewById(R.id.videoTitle);
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

    public class CacheDataSourceFactory implements DataSource.Factory {
        private final Context context;
        private final DefaultDataSourceFactory defaultDatasourceFactory;
        private final long maxFileSize, maxCacheSize;

        CacheDataSourceFactory(Context context, long maxCacheSize, long maxFileSize) {
            super();
            this.context = context;
            this.maxCacheSize = maxCacheSize;
            this.maxFileSize = maxFileSize;
            String userAgent = Util.getUserAgent(context, context.getString(R.string.app_name));
            DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            defaultDatasourceFactory = new DefaultDataSourceFactory(this.context,
                    bandwidthMeter,
                    new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter));
        }

        @Override
        public DataSource createDataSource() {
            LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(maxCacheSize);
            SimpleCache simpleCache = new SimpleCache(new File(context.getCacheDir(), "media"), evictor);
            return new CacheDataSource(simpleCache, defaultDatasourceFactory.createDataSource(),
                    new FileDataSource(), new CacheDataSink(simpleCache, maxFileSize),
                    CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null);
        }
    }

}
