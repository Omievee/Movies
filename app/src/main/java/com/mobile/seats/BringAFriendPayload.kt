package com.mobile.seats

import android.os.Parcelable
import com.mobile.model.Availability
import com.mobile.model.Screening
import com.mobile.model.Theater2
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BringAFriendPayload(
        var theater: Theater2,
        var screening: Screening,
        var availability: Availability
) : Parcelable