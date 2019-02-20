package com.mobile.plans

import io.reactivex.Single

interface PlansManager {

    fun getAvailablePlans(): Single<ChangePlanResponse>
    fun updateCurrentPlan(request:String):Single<ChangePlanResponse>
    fun checkCurrentBilling()
}

