package com.mobile.session

import com.mobile.UserPreferences
import com.mobile.network.Api
import com.mobile.network.BillingApi
import com.mobile.requests.AddressChangeRequest
import com.mobile.requests.ChangeEmailRequest
import com.mobile.requests.ChangePasswordRequest
import com.mobile.requests.CreditCardChangeRequest
import com.mobile.responses.ChangeEmailResponse
import com.mobile.responses.ChangePasswordResponse
import com.mobile.responses.UserInfoResponse
import com.mobile.rx.Schedulers
import io.reactivex.Single

class UserManagerImpl(val api: Api, val billingApi: BillingApi) : UserManager {


    private var userInfo: UserInfoResponse? = null

    override fun getUserInfo(): Single<UserInfoResponse> {
        return when (userInfo) {
            null -> api.getUserDataRx(UserPreferences.userId).doOnSuccess { userInfo = it }
            else -> Single.just(userInfo)
        }.compose(Schedulers.singleDefault())
    }


    override fun updateUserPassword(request: ChangePasswordRequest): Single<ChangePasswordResponse> {
        return api
                .changePassword(request)
                .compose(Schedulers.singleDefault())
    }

    override fun updateUserEmail(request: ChangeEmailRequest): Single<ChangeEmailResponse> {
        return api
                .changeEmail(request)
                .compose(Schedulers.singleDefault())
    }

    override fun updateAddress(userId: Int, request: AddressChangeRequest): Single<Any> {
        return api
                .updateAddress(userId, request)
                .compose(Schedulers.singleDefault())
    }
}