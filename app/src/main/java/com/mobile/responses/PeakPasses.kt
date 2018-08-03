package com.mobile.responses

import android.icu.text.TimeZoneNames
import android.os.Parcelable
import com.mobile.model.ParcelableDate
import com.mobile.model.asParcelableDate
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
import java.util.*


class PeakPassInfo(
        val peakPasses: List<PeakPass> = emptyList(),
        val enabled:Boolean=false,
        val nextRefillDate:String?=null
) {

    val currentPeakPass: PeakPass?
        get() {
            return peakPasses
                    .filter { enabled }
                    .sortedByDescending { it.expires }.firstOrNull()
        }

}

@Parcelize
class PeakPass(val id: Int = 0, val expires: ParcelableDate?=null) : Parcelable {

    fun expiresAsString(): String {
        if(expires==null) {
            return ""
        }
        return SimpleDateFormat("M/dd/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(expires)
    }

}
