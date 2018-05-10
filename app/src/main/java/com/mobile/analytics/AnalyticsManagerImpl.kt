package com.mobile.analytics

import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.LoginEvent
import com.mobile.model.User
import java.lang.String.valueOf

class AnalyticsManagerImpl : AnalyticsManager {

    override fun onUserLoggedIn(user: User) {
        Crashlytics.setUserEmail(user.email)
        Crashlytics.setUserIdentifier(valueOf(user.id))
        Answers.getInstance().logLogin(LoginEvent().putSuccess(true))
    }

    override fun onUserLoggedOut(user: User?) {

    }

}