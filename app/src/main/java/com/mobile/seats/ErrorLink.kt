package com.mobile.seats

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ErrorLink(val title:String, val key:String="default", val iconRes:Int?=null) : Parcelable