package com.mobile.plans

import com.mobile.ApiError
import io.reactivex.disposables.Disposable

class ChangePlansPresenter(val view: ChangePlansInt, val plansManager: PlansManager) {


    var plansDisposable: Disposable? = null
    var availablePlans: Array<PlanObject>? = null
    fun onCreate() {
        getPlans()
    }

    private fun getPlans() {
        plansDisposable?.dispose()

        plansDisposable = plansManager
                .getAvailablePlans()
                .subscribe({
                    availablePlans = it.availablePlans
                    view.updateAdapter(availablePlans)
                }, { error ->
                    when (error) {
                        is ApiError -> view.displayError()
                    }
                })
    }

    fun changePlan() {

    }

    fun cancelClicked() {
        view.displayCancellationFragment()
    }
}