package com.mobile.plans

import io.reactivex.Observable
import io.reactivex.Single

interface PlansManager {

    fun getAvailablePlans()
    fun upgradeCurrentPlan()
    fun downgradeCurrentPlan()
}

