package com.mobile.home

import com.mobile.model.PopInfo
import com.mobile.responses.MicroServiceRestrictionsResponse

interface HomeActivityView {
    fun logout()
    fun showTicketVerification(it: PopInfo)
    fun showSubscriptionButton(it: MicroServiceRestrictionsResponse)
}
