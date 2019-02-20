package com.mobile.network

import com.mobile.responses.RestrictionsResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface MicroApi {

    @GET("auth/v1/session/{userId}")
    fun getSession(@Path("userId") userId: Int): Single<RestrictionsResponse>
}