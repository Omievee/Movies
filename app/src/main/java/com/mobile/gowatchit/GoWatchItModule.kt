package com.mobile.gowatchit

import android.content.Context
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.mobile.application.Application
import com.mobile.network.ApiModule
import com.mobile.helpers.GoWatchItSingleton
import com.moviepass.BuildConfig
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [ApiModule::class])
class GoWatchItModule {

    companion object {
        const val GO_WATCH_IT = "go_watch_it"
    }

    @Provides
    @Singleton
    fun provideGoWatchItApi(
            @Named(GO_WATCH_IT) httpClient: OkHttpClient,
            gson: Gson): GoWatchItApi {
        return Retrofit.Builder()
                .baseUrl(BuildConfig.GO_WATCH_IT_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient)
                .build().create(GoWatchItApi::class.java);
    }

    @Provides
    @Singleton
    @Named(GO_WATCH_IT)
    fun provideOkHttpClient(
            cookieJar: GoWatchItCookieJar,
            loggingInterceptor: HttpLoggingInterceptor,
            apiKeyInterceptor: GoWatchItApiKeyInterceptor
    ): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        httpClient.connectTimeout(40, TimeUnit.SECONDS)
        httpClient.readTimeout(40, TimeUnit.SECONDS)
        httpClient.cookieJar(cookieJar)
        httpClient.addInterceptor(loggingInterceptor)
        httpClient.addInterceptor(apiKeyInterceptor)
        return httpClient.build()
    }

    @Provides
    @Singleton
    fun apiKeyInterceptor(): GoWatchItApiKeyInterceptor {
        return GoWatchItApiKeyInterceptor()
    }

    @Provides
    @Singleton
    fun goWatchItSingleton(goWatchItApi: GoWatchItApi): GoWatchItSingleton {
        return GoWatchItSingleton(goWatchItApi)
    }


    @Provides
    @Singleton
    fun provideCookieJar(application: Application): GoWatchItCookieJar {
        return GoWatchItCookieJar(application)
    }

    class GoWatchItCookieJar(context: Context) : PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))
}