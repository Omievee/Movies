package com.mobile.billing

interface MissingBillingFragmentView {
    fun showProgress()
    fun hideProgress()
    fun showErrors(valid: BillingInfo)
    fun startCardIOActivity()
}