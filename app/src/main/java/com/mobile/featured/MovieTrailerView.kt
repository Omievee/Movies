package com.mobile.featured

import android.content.Context
import android.net.Uri
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import com.mobile.Constants
import com.mobile.screening.MoviePosterClickListener
import com.mobile.model.Movie
import com.moviepass.R
import kotlinx.android.synthetic.main.list_item_featured_poster.view.*
import java.io.File

class MovieTrailerView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs), Player.EventListener {

    val player: SimpleExoPlayer

    var lastSource: String? = null

    var movie: Movie? = null

    var moviePosterClickListener: MoviePosterClickListener?=null

    init {
        inflate(context, R.layout.list_item_featured_poster, this)
        layoutParams = MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT)
        featuredPoster.minimumHeight = 9 * resources.getDisplayMetrics().heightPixels / 16
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
        player.addListener(this)
        featuredVideo.player = player
        this.setOnClickListener {
            Log.d(Constants.TAG, "clickclick: ")
            val movie = this.movie?: return@setOnClickListener
            moviePosterClickListener?.onMoviePosterClick(movie)
        }
    }

    fun bind(movie: Movie, enableVideoPlayback: Boolean = true) {
        this.movie = movie
        videoTitle.text = movie.title
        if (!enableVideoPlayback) {
            player.stop()
        }

        if (lastSource == movie.teaserVideoUrl) {
            if (player.playbackState == Player.STATE_READY) {
                player.playWhenReady
            }
            if(movie.teaserVideoUrl.isNullOrEmpty()) {
                onNoVideo()
            }
            return
        } else {
            onNoVideo()
        }
        val video = ExtractorMediaSource(Uri.parse(movie.teaserVideoUrl), CacheDataSourceFactory(context, (100 * 1024 * 1024).toLong(), (5 * 1024 * 1024).toLong()), DefaultExtractorsFactory(), null, null)
        player.prepare(video)
        player.playWhenReady = true
        player.volume = Player.DISCONTINUITY_REASON_INTERNAL.toFloat()
        player.repeatMode = Player.REPEAT_MODE_ONE

    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {

    }

    override fun onSeekProcessed() {
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {

    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        onNoVideo()
    }

    override fun onLoadingChanged(isLoading: Boolean) {

    }

    override fun onPositionDiscontinuity(reason: Int) {

    }

    override fun onRepeatModeChanged(repeatMode: Int) {

    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

    }

    fun onNoVideo() {
        featuredVideo.visibility = View.GONE
        featuredPoster.visibility = View.VISIBLE
        featuredPoster.setImageURI(movie?.landscapeImageUrl)
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playbackState == Player.STATE_READY) {
            featuredPoster.visibility = View.GONE
            videoFrameLayout.visibility = View.VISIBLE
        }
    }

    inner class CacheDataSourceFactory internal constructor(private val context: Context, private val maxCacheSize: Long, private val maxFileSize: Long) : DataSource.Factory {
        private val defaultDatasourceFactory: DefaultDataSourceFactory

        init {
            val userAgent = Util.getUserAgent(context, context.getString(R.string.app_name))
            val bandwidthMeter = DefaultBandwidthMeter()
            defaultDatasourceFactory = DefaultDataSourceFactory(this.context,
                    bandwidthMeter,
                    DefaultHttpDataSourceFactory(userAgent, bandwidthMeter))
        }

        override fun createDataSource(): DataSource {
            val evictor = LeastRecentlyUsedCacheEvictor(maxCacheSize)
            val simpleCache = SimpleCache(File(context.cacheDir, "media"), evictor)
            return CacheDataSource(simpleCache, defaultDatasourceFactory.createDataSource(),
                    FileDataSource(), CacheDataSink(simpleCache, maxFileSize),
                    CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null)
        }
    }
}
