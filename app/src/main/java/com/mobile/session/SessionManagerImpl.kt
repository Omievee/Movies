package com.mobile.session

import com.helpshift.util.HelpshiftContext
import com.mobile.UserPreferences
import com.mobile.model.User
import com.mobile.rx.Schedulers
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class SessionManagerImpl : SessionManager {

    val sub = PublishSubject.create<Boolean>()

    override fun loggedOut(): Observable<Boolean> {
        return sub.compose(Schedulers.observableDefault())
    }

    override fun getUser(): User? {
        val userId = UserPreferences.userId
        if (userId == 0) {
            return null;
        }
        val token = UserPreferences.authToken
        val oneDeviceId = UserPreferences.oneDeviceId
//        val androidId = UserPreferences.deviceAndroidID
        UserPreferences.firebaseHelpshiftToken
        return User(
                id = userId,
                authToken = token,
                email = UserPreferences.user.email,
                firstName = UserPreferences.user.firstName,
                lastName = UserPreferences.user.lastName
        )
    }

    override fun logout() {
        HelpshiftContext.getCoreApi().logout()
        UserPreferences.clearEverything()
        sub.onNext(true)
    }
}