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
        movieTime.text = movie.runningTime.runningTimeString(context)
        posterSPV.setImageURI(movie.landscapeImageUrl)
        when(synopsisListener) {
            null-> {
                synopsis.visibility = View.GONE
            }
            else-> {
                synopsis.visibility = View.VISIBLE
                synopsis.setOnClickListener {
                    synopsisListener.onMoviePosterClick(movie)
                }
            }
        }
    }

}