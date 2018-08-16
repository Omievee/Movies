package com.mobile.featured

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import com.facebook.drawee.backends.pipeline.Fresco
import com.mobile.screening.MoviePosterClickListener
import com.mobile.model.Movie
import com.moviepass.R
import com.mobile.adapters.MoviePostersAdapter.safeLongToInt
import kotlinx.android.synthetic.main.layout_vertical_movie_poster.view.*
import android.graphics.drawable.Animatable
import android.net.Uri
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import android.os.Build


class VerticalMoviePosterView(context: Context?,
                          attrs: AttributeSet? = null


) : ConstraintLayout(context, attrs) {

    var movie: Movie? = null
    var moviePosterClickListener: MoviePosterClickListener?=null

    init {
        inflate(context, R.layout.layout_vertical_movie_poster, this)
        try {
            val attrs = intArrayOf(R.attr.selectableItemBackground)
            val typedArray = context?.obtainStyledAttributes(attrs)
            val foregroundDrawable = typedArray?.getDrawable(0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                foreground = foregroundDrawable
            }
            typedArray?.recycle()
        } catch (e:Exception) {}
        val aspectW = 2
        val aspectH = 3

        val width = getResources().displayMetrics.widthPixels
        val finalWidth = width / 2.85
        val height = finalWidth * aspectH / aspectW

        val w = safeLongToInt(Math.round(finalWidth))
        val h = safeLongToInt(Math.round(height))

        layoutParams = MarginLayoutParams(w, h)
        setOnClickListener {
            val movie = this.movie?: return@setOnClickListener
            moviePosterClickListener?.onMoviePosterClick(movie)
        }
    }

    fun bind(movie: Movie, moviePosterClickListener: MoviePosterClickListener?=null) {
        this.movie = movie
        this.moviePosterClickListener = moviePosterClickListener
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
    }
}
