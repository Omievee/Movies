package com.mobile.home

import com.mobile.model.Alert
import com.mobile.model.LogoutInfo
import com.mobile.model.PopInfo
import com.mobile.model.ScreeningToken
import com.mobile.responses.MicroServiceRestrictionsResponse

interface HomeActivityView {
    fun logout()
    fun showTicketVerification(it: PopInfo)
    fun showSubscriptionButton(it: MicroServiceRestrictionsResponse)
    fun showAlert(it: Alert): Any
    fun showForceLogout(it: LogoutInfo)
    fun showConfirmationScreen(it: ScreeningToken)
}
