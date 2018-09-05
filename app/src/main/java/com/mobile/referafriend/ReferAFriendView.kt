package com.mobile.referafriend

import android.content.Intent

interface ReferAFriendView {

    fun showProgress()
    fun hideProgress()
    fun setReferralsInfo(title: String, message: String)
    fun showErrorDialog(message: String)
    fun showErrorDialog(message: Int)
    fun startEmailActivity(intent: Intent)
    fun showErrors(errors: Referral)
    fun removeErrors()
    fun showGenericError()
}