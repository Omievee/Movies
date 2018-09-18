package com.mobile.movie

import com.mobile.model.Movie
import com.mobile.responses.CurrentMoviesResponse
import io.reactivex.Single

interface MoviesManager {

    fun getAllMovies():Single<List<Movie>>
    fun getCurrentMovies():Single<CurrentMoviesResponse>
}