package com.mobile.movie

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.mobile.screening.MoviePosterClickListener
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_featured_container.view.*

class FeaturedView(context: Context?, attrs: AttributeSet?=null) : ConstraintLayout(context, attrs) {

    init {
        View.inflate(context, R.layout.layout_featured_container, this)
        layoutParams = MarginLayoutParams(MATCH_PARENT,WRAP_CONTENT)
    }

    fun bind(presentation: MoviesPresentation, moviePosterClickListener: MoviePosterClickListener?=null) {
        val movie = presentation.data.second.firstOrNull()?:return
        trailerView.bind(movie,enableVideoPlayback = true)
        trailerView.moviePosterClickListener = moviePosterClickListener
    }

}