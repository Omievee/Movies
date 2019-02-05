package com.mobile.plans

import io.reactivex.disposables.Disposable

class ChangePlansPresenter(val view: ChangePlansInt, val plansManager: PlansManager) {


    var plansDisposable:Disposable?=null
    fun onCreate() {

        getPlans()
    }

    private fun getPlans() {
        plansDisposable?.dispose()

      //  plansDisposable = plansManager.getAvailablePlans().equals()
    }

    fun changePlan() {

    }
}