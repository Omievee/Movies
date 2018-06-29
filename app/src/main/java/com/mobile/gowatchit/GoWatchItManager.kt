package com.mobile.gowatchit

import android.content.Context
import com.mobile.model.Screening
import com.mobile.model.Theater
import retrofit2.http.Url

interface GoWatchItManager {

    fun getCampaign(): String

    fun setCampaign(campaign:String)

    fun isAllMoviesEmpty(): Boolean

    fun userOpenedMovie(movieId: String, url: String, position:String)

    fun userClickedOnShowtime(theater: Theater, screening: Screening, showtime: String, movieId: String, url: String)

    fun checkInEvent(theater: Theater, screening: Screening, showTime: String, engagement: String, movieId: String, url: String)

    fun searchEvent(search: String, engagement: String, url: String)

    fun userOpenedApp(context: Context, deepLink:String)

    fun userOpenedTheater(theater: Theater, url: String)

    fun userOpenedTheaterTab(url: String, et: String)

    fun getMovies()
}