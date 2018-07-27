package com.mobile.gowatchit

import com.google.android.exoplayer2.C
import com.mobile.responses.GoWatchItResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface GoWatchItApi {

    @GET("/prod/ingest")
    fun send(@QueryMap map:Map<String,String>): Single<ResponseBody>
}