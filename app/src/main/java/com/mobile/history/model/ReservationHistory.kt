package com.mobile.history.model

import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize
import java.util.*

@RealmClass
@Parcelize
open class ReservationHistory(
        var id: Int? = null,
        var title: String? = null,
        var titleNormalized: String? = null,
        var imageUrl: String? = null,
        var userRating: String? = null,
        var theaterName: String? = null,
        var createdAt: Date? = null,
        var updatedAt: Date = Date()

) : RealmObject(), Parcelable {

    val rating: Rating
        get() {
            return Rating.values().find {
                it.name.toUpperCase() == userRating?.toUpperCase()
            } ?: Rating.UNKNOWN
        }

}

enum class Rating {
    GOOD, BAD, UNKNOWN
}