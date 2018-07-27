package com.mobile.gowatchit

import com.google.gson.annotations.SerializedName
import com.mobile.model.Theater

class OpenedTheater(theater: Theater): Base() {
    @SerializedName("ct")
    var clickThrough = "Theater"
    @SerializedName("ci")
    val engagementType = "-1"
    val theaterName = theater.name
    @SerializedName("thc")
    val theaterCity = theater.city
    @SerializedName("thr")
    val theaterState = theater.state
    @SerializedName("thz")
    val theaterZip = theater.zip
    @SerializedName("tha")
    val theaterAddress = theater.address
    @SerializedName("u")
    val url:String = "https://moviepass.com/go/theaters/${theater.tribuneTheaterId}"

}