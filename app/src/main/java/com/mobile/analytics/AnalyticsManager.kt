package com.mobile.analytics

import com.mobile.model.User

interface AnalyticsManager {

    fun onUserLoggedIn(user:User)
    fun onUserLoggedOut(user:User?)
}