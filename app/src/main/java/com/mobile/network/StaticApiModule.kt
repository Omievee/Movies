package com.mobile.network

import com.google.gson.Gson
import com.mobile.rx.RxJava2CallAdapterFactory
import com.mobile.session.SessionManager
import com.moviepass.BuildConfig
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class StaticApiModule {

    @Provides
    @Singleton
    @Static
    fun provideOkHttpClient(
            cache: Cache,
            sessionManager: SessionManager
    ): OkHttpClient.Builder {
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(AuthenticatedRequestInterceptor(sessionManager = sessionManager))
        httpClient.connectTimeout(20, TimeUnit.SECONDS)
        httpClient.readTimeout(45, TimeUnit.SECONDS)
        httpClient.cache(cache)
        return httpClient
    }

    @Singleton
    @Provides fun provideStaticApi(@Static client:OkHttpClient.Builder, gson:Gson): StaticApi {
        return Retrofit.Builder()
                .baseUrl(BuildConfig.STATIC_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client.build())
                .build()
                .create(StaticApi::class.java)
    }
}