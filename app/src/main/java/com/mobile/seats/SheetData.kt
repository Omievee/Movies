package com.mobile.seats

import android.os.Parcelable
import android.view.Gravity
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SheetData(val title: String, val description: String, val error: String?=null, val subDescription:String?=null, val gravity:Int= Gravity.CENTER,
                     val bottomErrorLink: ErrorLink?=null
                     ) : Parcelable