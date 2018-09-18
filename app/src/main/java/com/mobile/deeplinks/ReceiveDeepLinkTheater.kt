package com.mobile.deeplinks

import com.mobile.model.Theater

interface ReceiveDeepLinkTheater {

    fun onTheaterFromDeepLink(theater: Theater? = null)
}