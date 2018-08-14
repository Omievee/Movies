package com.mobile.alertscreen

import com.mobile.model.Alert

interface AlertScreenView {

    fun dismissAlertScreen(id: String?)
    fun hideDismissIcon()
    fun setAlert(alert: Alert)
    fun showRedConfirmButton(alert: Alert)
    fun hideRedConfirmButton()
    fun userClickedConfirmButton(alert: Alert)
    fun showFailureDialog()
    fun showWebLink()
    fun hideWebLink()

}