package com.mobile.network

import android.os.Build
import com.mobile.UserPreferences
import com.mobile.session.SessionManager
import com.moviepass.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class BoxOfficeAuthenticatedRequestInterceptor(val sessionManager: SessionManager) : Interceptor {

    companion object {
        private val USER_AGENT_VERSION by lazy {
            try {
                BuildConfig.VERSION_NAME.split(".")[0]
            } catch (e: Exception) {
                "3"
            }
        }
        val HEADER_ONE_DEVICE_ID = "M-Device-Id"
        val HEADER_USER_AGENT = Pair("User-Agent", "moviepass/android/${Build.VERSION.RELEASE}/v${USER_AGENT_VERSION}/${BuildConfig.VERSION_NAME}/${BuildConfig.VERSION_CODE}")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val user = sessionManager.getUser()
        val requestBuilder = original.newBuilder();
        user?.let {
            requestBuilder.apply {
                addHeader("M-User-Id", UserPreferences.restrictions.userUuid)
                addHeader("M-Auth-Token", it.authToken ?: "")
            }
        }
        requestBuilder.apply {
            addHeader(HEADER_USER_AGENT.first, HEADER_USER_AGENT.second)
        }
        UserPreferences.oneDeviceId?.let {
            requestBuilder.addHeader(HEADER_ONE_DEVICE_ID, it)
        }
        requestBuilder.addHeader("Content-Type", "application/json")
        requestBuilder.addHeader("Accept", "application/json")
        return chain.proceed(requestBuilder.build())
    }

}