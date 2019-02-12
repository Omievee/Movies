package com.mobile.plans

import java.util.*
import kotlin.collections.ArrayList

class ChangePlansResponse(val currentPlan: PlanObject,
                          val nextBillingDate: Date?,
                          val availablePlans: Array<PlanObject>
) {
}