package com.mobile.history.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.os.Parcelable
import com.mobile.model.Movie
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity
@Parcelize
open class ReservationHistory(
        @PrimaryKey
        var id: Int? = null,
        var title: String? = null,
        var imageUrl: String? = null,
        var userRating: String? = null,
        var theaterName: String? = null,
        var createdAt: Long? = null

) : Parcelable {
    @IgnoredOnParcel
    val rating: Rating
        get() {
            return Rating.values().find {
                it.name.toUpperCase() == userRating?.toUpperCase()
            } ?: Rating.UNKNOWN
        }

    @delegate:Ignore
    @IgnoredOnParcel
    val created by lazy {
        val createdAt = createdAt
        when (createdAt) {
            null -> null
            else -> Date(createdAt)
        }
    }

    fun toMovie(): Movie {
        return Movie(
                id = this.id ?: 0,
                title = this.title,
                imageUrl = this.imageUrl
        )
    }
}

enum class Rating {
    GOOD, BAD, UNKNOWN
}