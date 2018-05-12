package com.mobile.gowatchit

import okhttp3.Interceptor
import okhttp3.Response

class GoWatchItApiKeyInterceptor : Interceptor {

    companion object {
        val X_API_KEY = Pair("x-api-key", "Lalq1yYxOx2d1tj2VlOHw8fFXXUnih3a8TIHInHU")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestBuilder = original.newBuilder()
                .addHeader(X_API_KEY.first, X_API_KEY.second)
        val request = requestBuilder.build()
        return chain.proceed(request)
    }

}