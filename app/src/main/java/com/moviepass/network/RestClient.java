package com.moviepass.network;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.moviepass.Constants;
import com.moviepass.UserPreferences;

import java.io.IOException;
import java.util.StringTokenizer;
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

    static {
        System.loadLibrary("native-lib");
    }

    private native static String getEndPoint();

    static String url = String.valueOf(getEndPoint());

    private static Api sAuthenticatedAPI;
    private static Api sUnauthenticatedAPI;
    private static Retrofit sAuthenticatedInstance;
    private static Retrofit sUnauthenticatedInstance;

    public static int userId;
    public static String deviceUuid = "";
    public static String authToken = "";

    private RestClient() {
    }

    public static Api getAuthenticated() {
        return sAuthenticatedAPI;
    }

    public static Api getUnauthenticated() { return sUnauthenticatedAPI; }
    
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

                        .addHeader("user_id", "" + UserPreferences.getUserId())
                        .addHeader("device_uuid", UserPreferences.getDeviceUuid())
                        .addHeader("auth_token", UserPreferences.getAuthToken())
                        .addHeader("Content-type", "application/json")
                        .addHeader("Accept", "application/json")
                        .addHeader("User-Agent","MoviePass/Android/20170706");
                Request request = requestBuilder.build();

                return chain.proceed(request);
            }
        });

        sAuthenticatedInstance = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient.build())
                .build();
        sAuthenticatedAPI  = sAuthenticatedInstance.create(Api.class);
    }

    public static void setupUnauthenticatedWebClient(Context context) {
        sUnauthenticatedInstance = null;
        sUnauthenticatedAPI = null;

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
                        .addHeader("User-Agent","MoviePass/Android/20170703");
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        sUnauthenticatedInstance = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        sUnauthenticatedAPI  = sUnauthenticatedInstance.create(Api.class);
    }
}
