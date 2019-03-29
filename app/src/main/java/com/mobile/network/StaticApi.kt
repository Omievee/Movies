package com.mobile.network

import com.mobile.model.Movie
import com.mobile.responses.CurrentMoviesResponse
import com.mobile.responses.TheatersResponse
import io.reactivex.Single
import retrofit2.http.GET

interface StaticApi {
    /*ALL MOVIES FOR MAIN PAGE */
    @GET("movies/currentSelected.json")
    fun getAllCurrentMovies(): Single<CurrentMoviesResponse>


    /* ALL MOVIES FOR SEARCH */
    @GET("movies/all.json")
    fun getAllMovies(): Single<List<Movie>>


    /* ALL THEATERS */
    @GET("theaters/all.json")
    fun getAllMoviePassTheaters(): Single<TheatersResponse>

}