package com.mobile.profile

interface ProfileCancellationView {
    fun showProgress()
    fun hideProgress()
    fun showCancellationConfirmationDialog(reason: String, comment: String, billingDate: String?)
    fun successfullCancellation(billingDate: String?)
    fun unccessfullCancellation(message: String)
    fun bindView()
    fun showErrorMessage()
}