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
        return when (planInfo) {
            null -> {
                api
                        .getAvailablePlans()
                        .doOnSuccess {
                            planInfo = it
                        }
            }

            else -> Single
                    .just(planInfo)
        }
                .compose(
                        Schedulers
                                .singleDefault()
                )
    }

    override fun updateCurrentPlan(request: UpdatePlan): Single<Any> {
        return api
                .updateCurrentPlan(request)
                .doOnError {
                    it.printStackTrace()
                }
                .doOnSuccess {
                    //println("success")
                }
                .compose(Schedulers.singleDefault())
    }


}