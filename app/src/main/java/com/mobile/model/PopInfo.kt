package com.mobile.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by anubis on 7/17/17.
 */
@Parcelize
data class PopInfo(
        var reservationId: Int = 0,
        var movieTitle: String? = null,
        var theaterName: String? = null,
        var tribuneTheaterId: String? = null,
        var showtime: String? = null,
        var tribuneMovieId: String? = null
) : Parcelable