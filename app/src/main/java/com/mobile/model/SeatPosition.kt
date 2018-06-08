package com.mobile.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class SeatPosition(val row: Int, val column: Int) : Parcelable