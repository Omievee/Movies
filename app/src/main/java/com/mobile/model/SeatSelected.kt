package com.mobile.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by anubis on 6/20/17.
 */

@Parcelize
class SeatSelected(var selectedSeatRow: Int = 0,
                   var selectedSeatColumn: Int = 0,
                   var seatName:String?=null) : Parcelable