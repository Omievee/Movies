package com.mobile.plans

import com.mobile.network.Api
import com.mobile.network.BillingApi
import io.reactivex.Single

class PlansManagerImpl(val api: Api, val billingApi: BillingApi) : PlansManager {

    override fun getAvailablePlans(){

    }

    override fun upgradeCurrentPlan() {

    }

    override fun downgradeCurrentPlan() {

    }
}