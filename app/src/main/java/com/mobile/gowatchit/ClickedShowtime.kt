package com.mobile.gowatchit

import com.google.gson.annotations.SerializedName
import com.mobile.model.Availability
import com.mobile.model.Screening
import com.mobile.model.Theater

open class ClickedShowtime(theater: Theater, screening: Screening, availability: Availability) : Base() {
    @SerializedName("ct")
    var clickThrough = "Movie"
    @SerializedName("et")
    val engagementType = "theater_click"
    @SerializedName("tht")
    val showtime = availability.startTime?.filterNot { it == ' ' } ?: ""
    @SerializedName("tn")
    val theaterName = theater.name
    @SerializedName("thc")
    val theaterCity = theater.city
    @SerializedName("thr")
    val theaterState = theater.state
    @SerializedName("thz")
    val theaterZip = theater.zip
    @SerializedName("tha")
    val theaterAddress = theater.address
    @SerializedName("ci")
    val movieId = screening.moviepassId
    @SerializedName("cd")
    val movieTitle = screening.title
}