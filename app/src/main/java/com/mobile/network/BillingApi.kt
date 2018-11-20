package com.mobile.network

import com.mobile.billing.BillingInfo
import com.mobile.billing.Subscription
import com.mobile.requests.CancellationRequest
import com.mobile.responses.CancellationResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface BillingApi {

    @POST("subscriptions/v1/cancel/user")
    fun requestCancellation(@Body request: CancellationRequest): Single<CancellationResponse?>

    @PUT("subscriptions/v1/update/billing-info/user")
    fun updateBilling(@Body info: BillingInfo): Single<ResponseBody>

    @GET("subscriptions/v1/user")
    fun getSubscription(): Single<Subscription>

}