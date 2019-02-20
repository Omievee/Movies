package com.mobile.plans

interface ChangePlansInt {
    fun displayCancellationFragment()
    fun updateAdapter(current:PlanObject, plans: Array<PlanObject>?)
    fun displayError()
    fun planUpdateSuccess()


}