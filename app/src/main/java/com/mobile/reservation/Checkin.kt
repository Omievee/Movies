package com.mobile.reservation

import android.os.Parcelable
import com.mobile.model.Availability
import com.mobile.model.Screening
import com.mobile.model.Theater
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Checkin(
        var screening: Screening,
        var theater: Theater,
        var availability: Availability
) : Parcelable