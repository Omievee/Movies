package com.mobile.plans

import io.reactivex.Single

interface PlansManager {

    fun getAvailablePlans(): Single<ChangePlansResponse>
    fun updateCurrentPlan(request:String):Single<ChangePlansResponse>
    fun checkCurrentBilling()
}

