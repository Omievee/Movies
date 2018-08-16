package com.mobile.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Provider(var providerName: String? = null,
                    var theater: Int = 0,
                    var ticketType: String? = null,
                    var performanceInfo: Map<String, PerformanceInfo?>? = emptyMap()) : Parcelable
