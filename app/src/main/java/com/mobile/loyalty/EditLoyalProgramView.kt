package com.mobile.loyalty

interface EditLoyalProgramView {

    fun setLoyaltyData()
    fun showProgress()
    fun hideProgress()
    fun showUpdateLoyaltyFields()
    fun updateSuccessful(delete: Boolean)
    fun updateFailure(failure: String)
}