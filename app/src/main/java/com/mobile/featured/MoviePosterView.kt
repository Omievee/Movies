package com.mobile.featured

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.mobile.model.Movie
import com.moviepass.R
import kotlinx.android.synthetic.main.list_item_featured_poster.view.*

class MoviePosterView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    init {
        inflate(context, R.layout.list_item_featured_poster, this)
        layoutParams = MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT)
        featuredPoster.minimumHeight = 9 * resources.getDisplayMetrics().heightPixels / 16
    }

    fun bind(movie: Movie) {
        featuredPoster
                .setImageURI(movie.landscapeImageUrl)
        videoTitle.text = movie.title
    }
}