package com.mobile.plans

import com.mobile.network.Api
import com.mobile.network.BillingApi
import com.mobile.responses.UserInfoResponse
import com.mobile.rx.Schedulers
import io.reactivex.Scheduler
import io.reactivex.Single

class PlansManagerImpl(val api: Api, val billingApi: BillingApi) : PlansManager {


    private var plansInfo: ChangePlansResponse? = null

    override fun getAvailablePlans(): Single<ChangePlansResponse> {
        return when (plansInfo) {
            null -> api
                    .availablePlans
                    .doOnSuccess {
                        plansInfo = it
                    }

            else -> Single
                    .just(plansInfo)
        }
                .compose(
                        Schedulers
                                .singleDefault()
                )
    }

    override fun updateCurrentPlan(request: String): Single<ChangePlansResponse> {
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