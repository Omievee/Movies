package com.mobile.home

import com.mobile.model.PopInfo

interface HomeActivityView {
    fun logout()
    fun showTicketVerification(it: PopInfo)
}
