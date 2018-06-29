package com.mobile.session

import com.mobile.model.User

interface SessionManager {

    fun getUser(): User?
    fun logout()

}