package com.mobile.adapters

import android.app.Activity
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.mobile.model.Movie
import com.mobile.screening.MoviePosterClickListener
import com.moviepass.R
import kotlinx.android.synthetic.main.horizontal_poster.view.*
import kotlinx.android.synthetic.main.layout_screening_poster_header.view.*

class ScreeningPosterHeader(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    init {
        inflate(context, R.layout.layout_screening_poster_header, this)
        backButton.setOnClickListener {
            val activity = context as? Activity ?: return@setOnClickListener
            activity.onBackPressed()
        }
    }

    fun bind(movie:Movie, synopsisListener:MoviePosterClickListener?=null) {
        movieTitle.text = movie.title
        movieRating.text = movie.rating.toFormattedRating(context)
        movieRating.visibility = when(movieRating.text.isEmpty()) {
            true-> View.GONE
            else-> View.VISIBLE
        }
        movieTime.text = movie.runningTime.runningTimeString(context)
        spacer.visibility = movieRating.visibility
        posterSPV.setImageURI(movie.landscapeImageUrl)
        when(synopsisListener) {
            null-> {
                synopsisIV.visibility = View.GONE
            }
            else-> {
                synopsisIV.visibility = View.VISIBLE
                arrayOf(this).forEach {
                    setOnClickListener {
                        synopsisListener.onMoviePosterClick(movie)
                    }
                }
            }
        }
    }
}