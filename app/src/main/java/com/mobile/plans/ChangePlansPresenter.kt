package com.mobile.plans

import com.mobile.ApiError
import com.mobile.UserPreferences
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
                    view.updateAdapter(it.data.currentPlan, availablePlans?.toList())
                }, {
                    it.printStackTrace()
                })
    }


    fun changePlan(currentId:String?, plansUUID: String?) {
        val planId = UpdatePlan(plansUUID)
        if (planId.newPlanId == "" || plansUUID.equals(currentId)) {
            view.displayError("Select a new Plan")
            return
        }
        updateDisp?.dispose()
        updateDisp = plansManager
                .updateCurrentPlan(planId)
                .subscribe({
                    view.planUpdateSuccess("Plan Updated")
                }, { error ->
                    when (error) {
                        is ApiError -> {
                            view.displayError(err = error.error.message)
                        }
                    }
                }

                )
    }

    fun cancelClicked() {
        view.displayCancellationFragment()
    }

    fun onDestroy() {
        plansDisposable?.dispose()
        updateDisp?.dispose()
    }

    fun onResume() {
        getPlans()
    }

    fun displayBottomFragment(selected: PlanObject) {
        view.displayBottomSheetFragment(selected)
    }
}