package com.mobile.model

import android.os.Parcelable
import com.mobile.utils.text.toCurrency
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Surge(
        var level: SurgeType = SurgeType.WILL_SURGE,
        var amount: Int = 0,
        var screeningSurging: Boolean = false,
        val dependentUserSegments: List<Int> = emptyList(),
        val independentUserSegments: List<Int> = emptyList()
) : Parcelable {
    val costAsDollars: String
        get() {
            return amount.div(100.0).toCurrency()
        }

    companion object {
        val NONE: Surge = Surge(
                level = SurgeType.NO_SURGE
        )
    }
}