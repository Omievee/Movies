package com.mobile.loyalty

interface EditLoyalProgramView {

    fun updateLoyaltyProgramInfo()
    fun setLoyaltyData()
    fun showProgress()
    fun hideProgress()
    fun showUpdateLoyaltyFields()
    fun updateSuccessful()
    fun updateFailure(failure: String?)
    fun updateFailure(failure: Int)

}