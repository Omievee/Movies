package com.mobile.model

import android.os.Parcelable

import kotlinx.android.parcel.Parcelize


@Parcelize
class Reservation(
        var id: Int = 0,
        var expiration: Long = 0,
        var qrUrl: String? = null,
        var seats: List<String>? = null,
        var confirmationCode: String? = null
) : Parcelable