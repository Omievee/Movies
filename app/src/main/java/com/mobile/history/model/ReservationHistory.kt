package com.mobile.history.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.mobile.history.StarsRating.Rating
import com.mobile.model.Movie
import com.mobile.model.ParcelableDate
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Entity
@Parcelize
open class ReservationHistory(
        @PrimaryKey
        @SerializedName("movieId")
        var id: Int? = null,
        var title: String? = null,
        var imageUrl: String? = null,
        var showtime: String? = null,
        @SerializedName("rating")
        var userRating: String? = null,
        var theaterName: String? = null,
        @SerializedName("reviewedAt")
        var createdAt: String? = null

) : Parcelable {

    object DATE_FORMAT : SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.US)

    @IgnoredOnParcel
    val
            rating: Rating
        get() {
            return Rating.values().find {
                it.starRating == userRating
            } ?: Rating.UNKNOWN
        }

    @delegate:Ignore
    @IgnoredOnParcel
    val created: Long by lazy {
        val date: String = showtime ?: "0"
        when (date) {
            "0" -> 0
            else -> {
                var createdTime: Date? = null

                try {
                    createdTime = DATE_FORMAT.parse(date)
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
                createdTime?.time ?: 0
            }
        }
    }

    @delegate:Ignore
    @IgnoredOnParcel
    val showtimeDate: Date? by lazy {
        val date: String? = createdAt
        date ?: return@lazy null
        var createdTime: Date? = null

        try {
            createdTime = DATE_FORMAT.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        createdTime
    }


    fun toMovie(): Movie {
        return Movie(
                id = this.id ?: 0,
                title = this.title,
                imageUrl = this.imageUrl
        )
    }
}

