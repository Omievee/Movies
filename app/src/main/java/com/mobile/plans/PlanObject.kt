package com.mobile.plans

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class PlanObject(val id: String,
                 val name: String,
                 val lengthMonths: Int,
                 val installmentAmount: Int,
                 val signUpFee: Int?,
                 val cap: Int,
                 val shouldRenew: Boolean,
                 val zone: Int?,
                 val features: Array<String> = emptyArray(),
                 val currentPlan: PlanObject,
                 val availablePlans: Array<PlanObject> = emptyArray()
) : Parcelable {
}