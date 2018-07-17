package com.mobile.featured

import android.app.Activity
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.mobile.screening.MoviePosterClickListener
import com.mobile.helpers.LogUtils
import com.mobile.model.Movie
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_screening_poster_header.view.*
import kotlinx.android.synthetic.main.list_item_featured_poster.view.*

class MoviePosterView(context: Context?,
                      attrs: AttributeSet? = null,
                      var moviePosterClickListener: MoviePosterClickListener?=null

) : ConstraintLayout(context, attrs) {

    var movie: Movie? = null

    init {
        inflate(context, R.layout.list_item_featured_poster, this)
        layoutParams = MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT)
        featuredPoster.minimumHeight = 9 * resources.getDisplayMetrics().heightPixels / 16
        this.setOnClickListener {
            val movie = this.movie?: return@setOnClickListener
            moviePosterClickListener?.onMoviePosterClick(movie)
        }
    }

    fun bind(movie: Movie) {
        featuredPoster.setImageURI(movie.landscapeImageUrl)
        videoTitle.text = movie.title
    }
}
