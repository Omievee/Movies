package com.mobile.session

import com.mobile.responses.UserInfoResponse
import io.reactivex.Single

interface UserManager {
    fun getUserInfo(): Single<UserInfoResponse>

    fun updateUserInfo()
}