package com.mobile.billing

import android.widget.EditText

interface MissingBillingFragmentView {
    fun showProgress()
    fun hideProgress()
    fun showErrors(valid: BillingInfo)
    fun startCardIOActivity()
    fun showBillingAddress(billingInfo: BillingInfo)
    fun showBillingCreditCard(billingInfo: BillingInfo)
    fun showSaveAndCancel()
    fun hideSaveAndCancel()
    fun setUpTextWatchers()
    fun clearFocus()
    fun showErrorDialog(message: String)
    fun showSuccessDialog(message: Int)
    fun showGenericError()
    fun clearText(editText: EditText)
    fun hideKeyboard()
    fun hideErrors()
}