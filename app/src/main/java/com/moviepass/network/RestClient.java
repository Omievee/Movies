package com.moviepass.network;

import android.content.Context;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.moviepass.Constants;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * This class creates 2 rest clients. A simple client and an authenticated one, which adds some
 * additional headers to each request. As of the time of writing this class, the server-side API
 * has authenticated and non-authenticated endpoints with no logic. Hopefully there will be a
 * refactor in the near future and the only non-authenticated endpoints will be those related to
 * sign and signup. Until then, we use this approach.
 */
public class RestClient {

    private static Api sAuthenticatedAPI;
    private static Api sSimpleAPI;
    private static Retrofit sAuthenticatedInstance;
    private static Retrofit sSimpleInstance;

    /* TODO REMOVE GENERIC */

    public static int userId = 2041;
    public static String deviceUuid = "D999236A42B8C9AE71201E18058940C2A6AACC73";
    public static String authToken = "W9MdGCulq64Jw1KKjK/QrxldKy+vn6QV9trDQXQEGxU=";

    private RestClient() {
    }

    public static Api getAuthenticated() {
        return sAuthenticatedAPI;
    }

    public static Api get() {
        return sSimpleAPI;
    }

    public static void setupAuthenticatedWebClient(Context context) {
        sAuthenticatedInstance = null;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        if (Constants.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.connectTimeout(40, TimeUnit.SECONDS);
        httpClient.readTimeout(40, TimeUnit.SECONDS);
        httpClient.addInterceptor(logging);

        CookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));

        httpClient.cookieJar(cookieJar);
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();

                // Request customization: add request headers
                Request.Builder requestBuilder = original.newBuilder()

                        .addHeader("user_id", "" + userId)
                        .addHeader("device_uuid", deviceUuid)
                        .addHeader("auth_token", authToken)
                        .addHeader("Content-type", "application/json")
                        .addHeader("Accept", "application/json")
                        .addHeader("User-Agent","MoviePass/Android/20161221");
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        sAuthenticatedInstance = new Retrofit.Builder()
                .baseUrl(Constants.ENDPOINT)

                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        sAuthenticatedAPI  = sAuthenticatedInstance.create(Api.class);
    }

    public static void setupSimpleRestClient(Context context) {
        sSimpleInstance = null;
        sSimpleAPI = null;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        if (Constants.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.connectTimeout(40, TimeUnit.SECONDS);
        httpClient.readTimeout(40, TimeUnit.SECONDS);
        httpClient.addInterceptor(logging);

        CookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));

        httpClient.cookieJar(cookieJar);
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();

                // Request customization: add request headers
                Request.Builder requestBuilder = original.newBuilder()
                        .addHeader("user_id", "" + userId)
                        .addHeader("device_uuid", deviceUuid)
                        .addHeader("auth_token", authToken)
                        .addHeader("Content-type", "application/json")
                        .addHeader("Accept", "application/json")
                        .addHeader("User-Agent","MoviePass/Android/20170519");
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        sSimpleInstance = new Retrofit.Builder()
                .baseUrl(Constants.ENDPOINT)

                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        sSimpleAPI  = sSimpleInstance.create(Api.class);
    }

}
