package com.mobile.featured

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import com.facebook.drawee.backends.pipeline.Fresco
import com.mobile.screening.MoviePosterClickListener
import com.mobile.model.Movie
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_vertical_movie_poster.view.*
import android.graphics.drawable.Animatable
import android.net.Uri
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import android.os.Build
import android.view.View
import com.mobile.UserPreferences
import com.mobile.listeners.BonusMovieClickListener


open class VerticalMoviePosterView(
        context: Context?,
        attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    var movie: Movie? = null
    var moviePosterClickListener: MoviePosterClickListener? = null
    var bonusClickListener: BonusMovieClickListener? = null

    init {
        inflate(context, R.layout.layout_vertical_movie_poster, this)
        val aspectW = 2
        val aspectH = 3

        val width = resources.displayMetrics.widthPixels
        val finalWidth = width / 2.85
        val height = finalWidth * aspectH / aspectW

        val w = Math.round(finalWidth).toInt()
        val h = Math.round(height).toInt()

        layoutParams = MarginLayoutParams(w, h)

        whiteListBanner.setOnClickListener {
            bonusClickListener?.onBonusBannerClickListener()
        }

        setOnClickListener {
            val movie = this.movie ?: return@setOnClickListener
            moviePosterClickListener?.onMoviePosterClick(movie)
        }
    }

    fun bind(movie: Movie, moviePosterClickListener: MoviePosterClickListener? = null, bonusMovieClickListener: BonusMovieClickListener?=null) {
        this.movie = movie
        this.moviePosterClickListener = moviePosterClickListener
        this.bonusClickListener = bonusMovieClickListener
        val imgUrl = Uri.parse(movie.imageUrl)
        val request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                .setProgressiveRenderingEnabled(true)
                .setResizeOptions(ResizeOptions(1080, 1920))
                .setSource(imgUrl)
                .build()
        title.text = ""
        val controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(ticket_top_red_dark.controller)
                .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                    override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
                        super.onFinalImageSet(id, imageInfo, animatable)
                        if (imgUrl.toString().contains("default")) {
                            title.text = movie.title
                        }
                    }

                    override fun onFailure(id: String?, throwable: Throwable?) {
                        ticket_top_red_dark.setImageURI(movie.imageUrl + "/original.jpg")
                    }
                })
                .build()

        ticket_top_red_dark.controller = controller
        showWhiteListMovieBanner()
    }

    private fun showWhiteListMovieBanner() {
        whiteListBanner.visibility = when (UserPreferences.restrictions.capWhitelistedMovieIds.contains(movie?.id
                ?: 0)) {
            true -> View.VISIBLE
            else -> View.INVISIBLE
        }
    }
}
