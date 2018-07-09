package com.mobile.seats

import android.os.Parcelable
import com.mobile.model.Availability
import com.mobile.model.Screening
import com.mobile.model.Theater
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BringAFriendPayload(
        var theater: Theater,
        var screening: Screening,
        var availability: Availability
) : Parcelable