package com.mobile.gowatchit

import com.google.gson.annotations.SerializedName
import com.mobile.model.Movie

class MovieImpression(movie: Movie) : Base() {

    init {
        event = "impression"
    }

    @SerializedName("ct")
    val clickThrough = "Movie"

    @SerializedName("ci")
    val movieId = movie.id

}
