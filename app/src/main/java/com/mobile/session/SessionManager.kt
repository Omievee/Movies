package com.mobile.session

import com.mobile.model.User
import io.reactivex.Observable

interface SessionManager {

    fun loggedOut(): Observable<Boolean>
    fun getUser(): User?
    fun logout()

}