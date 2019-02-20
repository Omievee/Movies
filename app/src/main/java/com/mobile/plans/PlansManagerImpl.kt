package com.mobile.plans

import com.mobile.UserPreferences
import com.mobile.network.Api
import com.mobile.network.BillingApi
import com.mobile.rx.Schedulers
import io.reactivex.Single
import java.util.*

class PlansManagerImpl(val api: Api, val billingApi: BillingApi) : PlansManager {
    private var planInfo: ChangePlanResponse? = null

    override fun getAvailablePlans(): Single<ChangePlanResponse> {
        println(">>>>>>>>>>>>>>>>>>>>>>> START GET OF PLANS")
        return api
                .getAvailablePlans(UserPreferences.user.UUID)
                .doOnSuccess {
                    println(">>>>>>>>>>>>>>>>>>>>>>>${it.data.currentPlan.name}")
                    planInfo = it
                }
                .doOnError {
                    println("Error is bullshit" + it.message)
                }

//        return when (planInfo) {
//            null ->
//
//
//            else -> Single
//                    .just(planInfo)
//        }
//                .compose(
//                        Schedulers
//                                .singleDefault()
//                )
    }

    override fun updateCurrentPlan(request: String): Single<ChangePlanResponse> {
        return api
                .updateCurrentPlan("user", request)
                .compose(
                        Schedulers
                                .singleBackground()
                )
    }



    override fun checkCurrentBilling() {

    }
}