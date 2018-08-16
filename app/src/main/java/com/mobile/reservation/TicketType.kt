package com.mobile.reservation

import android.os.Parcelable

import kotlinx.android.parcel.Parcelize

@Parcelize
class TicketType(var showtimeId: String? = null,
                 var id: String? = null,
                 var quantity: Int = 0,
                 var price: Int = 0
) : Parcelable
