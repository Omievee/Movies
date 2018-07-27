package com.mobile.gowatchit

import com.google.gson.annotations.SerializedName

class MovieSearch(@SerializedName("tr") val query: String) : Base() {

    @SerializedName("e")
    override var event = "search"
    @SerializedName("ct")
    val clickThrough = "Movie"
    @SerializedName("ci")
    val clearIt = -1
    var url:String = "https://moviepass.com/go/movies"

}
