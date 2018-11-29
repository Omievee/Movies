package com.mobile.network

import com.mobile.history.response.ReservationHistoryResponse
import com.mobile.requests.RatingRequest
import com.mobile.responses.HistoryResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface GwApi{


    @GET("/gw/review-mpcore/user/redemption/history")
    fun getReservationHistory(): Single<ReservationHistoryResponse>

    @POST("/gw/review-mpcore/user/review")
    fun submitRatingV2(@Body request: RatingRequest): Single<HistoryResponse>
}