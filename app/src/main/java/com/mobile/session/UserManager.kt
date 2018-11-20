package com.mobile.session

import com.mobile.requests.AddressChangeRequest
import com.mobile.requests.ChangeEmailRequest
import com.mobile.requests.ChangePasswordRequest
import com.mobile.responses.ChangeEmailResponse
import com.mobile.responses.ChangePasswordResponse
import com.mobile.responses.UserInfoResponse
import io.reactivex.Single

interface UserManager {

    fun getUserInfo(): Single<UserInfoResponse>
    fun updateUserEmail(request: ChangeEmailRequest): Single<ChangeEmailResponse>
    fun updateUserPassword(request: ChangePasswordRequest): Single<ChangePasswordResponse>
    fun updateAddress(userId: Int, request: AddressChangeRequest): Single<Any>
}