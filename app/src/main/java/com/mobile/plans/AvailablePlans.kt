package com.mobile.plans

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class AvailablePlans(val id: String?,
                     val name: String?,
                     val price: Double?,
                     val features: Array<String> = emptyArray()
) : Parcelable {
}