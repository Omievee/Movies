package com.mobile.session

import com.mobile.UserPreferences
import com.mobile.network.Api
import com.mobile.responses.UserInfoResponse
import com.mobile.rx.Schedulers
import io.reactivex.Single

class UserManagerImpl(val api:Api) : UserManager {

    private var userInfo: UserInfoResponse? = null

    override fun getUserInfo(): Single<UserInfoResponse> {
        return when (userInfo) {
            null -> api.getUserDataRx(UserPreferences.userId).doOnSuccess { userInfo = it }
            else -> Single.just(userInfo)
        }.compose(Schedulers.singleDefault())
    }

}