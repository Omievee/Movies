package com.mobile.session

import com.mobile.UserPreferences
import com.mobile.model.User
import com.mobile.network.Api
import com.mobile.responses.UserInfoResponse
import com.mobile.rx.Schedulers
import io.reactivex.Single

class SessionManagerImpl() : SessionManager {

    override fun getUser(): User? {
        val userId = UserPreferences.userId
        if (userId == 0) {
            return null;
        }
        val token = UserPreferences.authToken
        val oneDeviceId = UserPreferences.oneDeviceId
        val email = UserPreferences.userEmail
        val androidId = UserPreferences.deviceAndroidID
        UserPreferences.firebaseHelpshiftToken
        return User(
                id = userId,
                authToken = token,
                oneDeviceId = oneDeviceId,
                email = email,
                androidID = androidId
        )
    }

    override fun logout() {
        UserPreferences.clearEverything()
    }
}