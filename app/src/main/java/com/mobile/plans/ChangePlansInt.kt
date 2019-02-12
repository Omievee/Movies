package com.mobile.plans

interface ChangePlansInt {
    fun displayCancellationFragment()
    fun updateAdapter(plans: Array<PlanObject>?)
    fun displayError()

}