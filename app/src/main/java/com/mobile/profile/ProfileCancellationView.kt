package com.mobile.profile

import java.util.*

interface ProfileCancellationView {
    fun showProgress()
    fun hideProgress()
    fun showCancellationConfirmationDialog(reason: String, comment: String, date: Date?)
    fun successfullCancellation(billingDate: String?)
    fun unccessfullCancellation(message: String)
    fun bindView()
    fun showErrorDialog(error: Int)
}