package com.mobile

import android.widget.ImageView

import com.mobile.model.Movie
import com.mobile.model.Screening

/**
 * Created by ryan on 4/27/17.
 */

interface MoviePosterClickListener {

    fun onMoviePosterClick(movie: Movie, sharedImageView: ImageView)
}