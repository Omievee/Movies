package com.mobile.plans

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.text.DecimalFormat

data class ChangePlanResponse(val data: ChangePlanData)

class ChangePlanData(val newPlanId: String,
                     val currentPlan: PlanObject,
                     val availablePlans: List<PlanObject> = emptyList()
)


@Parcelize
class PlanObject(val id: String,
                 val name: String,
                 val installmentAmount: Int,
                 val features: Array<String> = emptyArray()

) : Parcelable {
    val asDollars: String
        get() {
            return installmentAmount.div(100.00).toString()
        }


}

data class UpdatePlan(val newPlanId: String?)

class UpdatePlanResponse(val data:Any)


