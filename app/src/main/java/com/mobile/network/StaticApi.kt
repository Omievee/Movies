package com.mobile.network

import com.mobile.responses.AllMoviesResponse
import com.mobile.responses.LocalStorageMovies
import com.mobile.responses.TheatersResponse
import io.reactivex.Single
import retrofit2.http.GET

interface StaticApi {
    /*ALL MOVIES FOR MAIN PAGE */
    @GET("movies/current.json")
    fun getAllCurrentMovies(): Single<LocalStorageMovies>


    /* ALL MOVIES FOR SEARCH */
    @GET("movies/all.json")
    fun getAllMovies(): Single<List<AllMoviesResponse>>


    /* ALL THEATERS */
    @GET("theaters/all.json")
    fun getAllMoviePassTheaters(): Single<TheatersResponse>

}