package com.mobile.plans

interface ChangePlansInt {
    fun displayCancellationFragment()
    fun updateAdapter(current: PlanObject, planToUse:PlanObject, plans: List<PlanObject>)
    fun displayError(err:String)
    fun planUpdateSuccess(msg:String)
    fun displayBottomSheetFragment(selectedPlan:PlanObject)
}