package com.mobile.screening

import com.mobile.model.Movie
import com.mobile.model.Screening

interface MoviePosterClickListener {

    fun onMoviePosterClick(movie: Movie? = null, screening: Screening? = null)
}