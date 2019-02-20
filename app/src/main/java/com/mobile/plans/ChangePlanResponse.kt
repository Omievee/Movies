package com.mobile.plans

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.text.DecimalFormat

data class ChangePlanResponse(val data: ChangePlanData)

class ChangePlanData(val newPlanId: String,
                     val currentPlan: PlanObject,
                     val availablePlans: Array<PlanObject> = emptyArray()
)


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
    val asDollars: String
        get() {
            return installmentAmount.div(100.00).toString()
        }


}

data class UpdatePlan(val newPlanId: String?)

class UpdatePlanResponse(val data:Any)


