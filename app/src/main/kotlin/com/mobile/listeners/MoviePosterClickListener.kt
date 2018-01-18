package com.mobile

import android.widget.ImageView

import com.mobile.model.Movie

/**
 * Created by ryan on 4/27/17.
 */

interface MoviePosterClickListener {

    fun onMoviePosterClick(pos: Int, movie: Movie, sharedImageView: ImageView)
}
