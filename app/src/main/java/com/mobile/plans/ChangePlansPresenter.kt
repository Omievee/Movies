package com.mobile.plans

import com.mobile.ApiError
import io.reactivex.disposables.Disposable

class ChangePlansPresenter(val view: ChangePlansInt, val plansManager: PlansManager) {

    var plansDisposable: Disposable? = null
    var updateDisp: Disposable? = null
    var availablePlans: Array<PlanObject>? = null

    fun onCreate() {
        getPlans()
    }

    private fun getPlans() {
        plansDisposable?.dispose()
        plansDisposable = plansManager
                .getAvailablePlans()
                .subscribe({
                    availablePlans = it.data.availablePlans
                    view.updateAdapter(it.data.currentPlan, availablePlans)
                }, {
                    it.printStackTrace()
                })
    }


    fun changePlan(plansUUID: String) {
        if (plansUUID == "") {
            view.displayError()
        }
        updateDisp?.dispose()
        updateDisp = plansManager
                .updateCurrentPlan(plansUUID)
                .subscribe({
                    view.planUpdateSuccess()
                }, { error ->
                    when (error) {
                        is ApiError -> view.displayError()
                    }
                })
    }

    fun cancelClicked() {
        view.displayCancellationFragment()
    }

    fun destroyEverything() {
        plansDisposable?.dispose()
        updateDisp?.dispose()
    }

    fun onResume() {
        getPlans()
    }
}