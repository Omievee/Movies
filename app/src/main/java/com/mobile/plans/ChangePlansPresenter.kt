package com.mobile.plans

import com.mobile.ApiError
import com.mobile.UserPreferences
import io.reactivex.disposables.Disposable

class ChangePlansPresenter(val view: ChangePlansInt, val plansManager: PlansManager) {

    var plansDisposable: Disposable? = null
    var updateDisp: Disposable? = null
    var availablePlans: List<PlanObject>? = null
    var planToUse:PlanObject? = null
    var currentPlan:PlanObject?=null

    fun onCreate() {
        getPlans()
    }

    fun onPlanSelected(plan:PlanObject) {
        planToUse=plan
        view.updateAdapter(currentPlan!!,planToUse!!,availablePlans!!)
    }

    private fun getPlans() {
        plansDisposable?.dispose()
        plansDisposable = plansManager
                .getAvailablePlans()
                .subscribe({
                    availablePlans = it.data.availablePlans
                    currentPlan = it.data.currentPlan
                    view.updateAdapter(currentPlan!!, currentPlan!!, availablePlans!!)
                }, {
                    it.printStackTrace()
                })
    }


    fun changePlan() {
        val planId = UpdatePlan(planToUse?.id?:return)
        if (planId.newPlanId == "" || currentPlan?.id==planToUse?.id) {
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

    fun onChangePlansClicked() {
        view.displayBottomSheetFragment(planToUse!!)
    }
}