package com.mobile.network

import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mobile.application.Application
import com.mobile.model.CapType
import com.mobile.model.ParcelableDate
import com.mobile.model.SurgeType
import com.mobile.responses.CurrentMoviesResponse
import com.mobile.rx.RxJava2CallAdapterFactory
import com.mobile.session.SessionManager
import com.moviepass.BuildConfig
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class ApiModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
            cookieJar: CookieJar,
            loggingInterceptor: HttpLoggingInterceptor,
            authenticatedRequestInterceptor: AuthenticatedRequestInterceptor,
            cache: Cache
    ): OkHttpClient.Builder {
        val httpClient = OkHttpClient.Builder()
        httpClient.connectTimeout(40, TimeUnit.SECONDS)
        httpClient.readTimeout(40, TimeUnit.SECONDS)
        httpClient.cookieJar(cookieJar)
        httpClient.addInterceptor(authenticatedRequestInterceptor)
        httpClient.addInterceptor(loggingInterceptor)
        return httpClient
    }

    @Provides
    @Singleton
    @Zuora
    fun provideZuoraOkHttpClient(
            cookieJar: CookieJar,
            loggingInterceptor: HttpLoggingInterceptor,
            authenticatedRequestInterceptor: BoxOfficeAuthenticatedRequestInterceptor,
            cache: Cache
    ): OkHttpClient.Builder {
        val httpClient = OkHttpClient.Builder()
        httpClient.connectTimeout(40, TimeUnit.SECONDS)
        httpClient.readTimeout(40, TimeUnit.SECONDS)
        httpClient.cookieJar(cookieJar)
        httpClient.addInterceptor(authenticatedRequestInterceptor)
        httpClient.addInterceptor(loggingInterceptor)
        return httpClient
    }

    @Provides
    @Singleton
    fun provideAuthenticatedRequestInterceptor(sessionManager: SessionManager): AuthenticatedRequestInterceptor {
        return AuthenticatedRequestInterceptor(sessionManager)
    }

    @Provides
    @Singleton
    fun provideBoxOfficeAuthenticatedRequestInterceptor(sessionManager: SessionManager): BoxOfficeAuthenticatedRequestInterceptor {
        return BoxOfficeAuthenticatedRequestInterceptor(sessionManager)
    }

    @Provides
    @Singleton
    fun provideHttpLogging(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        return logging
    }

    @Provides
    @Singleton
    fun provideCookieJar(application: Application): CookieJar {
        return PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(application))
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        lateinit var gson2: Gson
        val gson = GsonBuilder()
                .setLenient()
                .registerTypeAdapter(object : TypeToken<ParcelableDate>() {
                }.type, DateAdapter())
                .registerTypeAdapter(object : TypeToken<Date>() {
                }.type, DateAdapter())
                .registerTypeAdapter(object : TypeToken<SurgeType>() {

                }.type, SurgeTypeAdapter())
                .registerTypeAdapter(object : TypeToken<CapType>() {

                }.type, CapTypeAdapter())
                .registerTypeAdapter(object : TypeToken<RestrictionsCheckType>() {

                }.type, RestrictionCapTypeAdapter())
                .registerTypeAdapter(object : TypeToken<CurrentMoviesResponse>() {

                }.type, object : CurrentMoviesResponseTypeAdapter() {
                    override fun gson(): Gson {
                        return gson2
                    }
                })
                .create()
        gson2 = gson
        return gson
    }

    @Provides
    @Singleton
    fun provideCache(application: Application): Cache {
        return Cache(application.cacheDir, 10 * 1024 * 1024)
    }

    @Provides
    @Singleton
    fun provideApi(client: OkHttpClient.Builder, gson: Gson): Api {
        return Retrofit.Builder()
                .baseUrl(BuildConfig.baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client.build())
                .build()
                .create(Api::class.java)
    }

    @Provides
    @Singleton
    fun provideMicroApi(client: OkHttpClient.Builder, gson: Gson): MicroApi {
        return Retrofit.Builder()
                .baseUrl(BuildConfig.microServiceURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client.build())
                .build()
                .create(MicroApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBillingApi(@Zuora client: OkHttpClient.Builder, gson: Gson): BillingApi {
        return Retrofit.Builder()
                .baseUrl(BuildConfig.ZUORA_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client.build())
                .build()
                .create(BillingApi::class.java)
    }
}