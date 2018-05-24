package com.mobile.history.model

import io.realm.RealmObject
import io.realm.annotations.RealmClass
import java.util.Date

@RealmClass
open class ReservationHistory(
        var id: Int? = null,
        var title: String? = null,
        var titleNormalized: String? = null,
        var landscapeImageUrl: String? = null,
        var userRating:String? = null,
        var theaterName: String? = null,
        var createdAt: Date? = null,
        var updatedAt: Date = Date()
) : RealmObject()