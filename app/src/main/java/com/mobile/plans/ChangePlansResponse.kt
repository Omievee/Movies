package com.mobile.plans

import java.util.*
import kotlin.collections.ArrayList

class ChangePlansResponse(val currentPlan: AvailablePlans?,
                          val nextBillingDate: Date?,
                          val availablePlans: ArrayList<AvailablePlans>?
) {
}