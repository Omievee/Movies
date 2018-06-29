package com.mobile.session

import com.mobile.UserPreferences
import com.mobile.model.User

class SessionManagerImpl : SessionManager {
    override fun getUser(): User? {
        val userId = UserPreferences.getUserId()
        if (userId == 0) {
            return null;
        }
        val token = UserPreferences.getAuthToken()
        val oneDeviceId = UserPreferences.getOneDeviceId()
        val email = UserPreferences.getUserEmail()
        val androidId = UserPreferences.getDeviceAndroidID()
        UserPreferences.getFirebaseHelpshiftToken()
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