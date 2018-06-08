package com.mobile.seats

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SheetData(val title: String, val description: String, val error: String?=null) : Parcelable