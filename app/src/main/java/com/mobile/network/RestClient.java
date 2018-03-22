package com.mobile.network;

import android.content.Context;
import android.util.Log;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mobile.Constants;
import com.mobile.UserPreferences;

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

    static {
        System.loadLibrary("native-lib");
    }

    private native static String getEndPoint();

    static String a1URL = "http://a1.moviepass.com ";
    static String baseURL = String.valueOf(getEndPoint());
    static String registrationURL = "https://registration.moviepass.com/";
    static String staticRegistrationURL = "https://registration-stg.herokuapp.com";

    private static Api sAuthenticatedAPI;
    private static Api sAuthenticatedAPIGoWatchIt;
    private static Api sUnauthenticatedAPI;
    private static Api sAuthenticatedRegistrationAPI;
    private static Api sAuthenticatedStagingRegistrationAPI;

    private static Retrofit sAuthenticatedInstance;
    private static Retrofit sAuthenticatedInstanceGoWatchIt;
    private static Retrofit sUnauthenticatedInstance;
    private static Retrofit localStorageInstance;
    private static Retrofit sAuthenticatedRegistrationInstance;
    private static Retrofit sAuthenticatedStagingRegistrationInstance;

    private static Api localStorageAPI;

    public static int userId;
    public static String deviceUuid = "";
    public static String authToken = "";

    private RestClient() {
    }

    public static Api getAuthenticated() {
        return sAuthenticatedAPI;
    }


    public static Api getsAuthenticatedRegistrationAPI() {
        return sAuthenticatedRegistrationAPI;
    }

    public static Api getAuthenticatedAPIGoWatchIt() {
        return sAuthenticatedAPIGoWatchIt;

    }

    public static Api getUnauthenticated() {
        return sUnauthenticatedAPI;
    }

    public static Api getLocalStorageAPI() {
        return localStorageAPI;
    }

    public static Api getsAuthenticatedStagingRegistrationAPI() {
        return sAuthenticatedStagingRegistrationAPI;
    }

    public static void setupAuthenticatedStagingRegistrationClient(Context context) {

        sAuthenticatedStagingRegistrationAPI = null;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        if (Constants.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.connectTimeout(20, TimeUnit.SECONDS);
        httpClient.readTimeout(20, TimeUnit.SECONDS);
        httpClient.addInterceptor(logging);

        CookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));

        httpClient.cookieJar(cookieJar);
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();
                // Request customization: add request headers
                Request.Builder requestBuilder = original.newBuilder();
                Request request = requestBuilder.build();

                return chain.proceed(request);
            }
        });

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        sAuthenticatedStagingRegistrationInstance = new Retrofit.Builder()
                .baseUrl(staticRegistrationURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();
        sAuthenticatedStagingRegistrationAPI = sAuthenticatedStagingRegistrationInstance.create(Api.class);
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
        httpClient.connectTimeout(20, TimeUnit.SECONDS);
        httpClient.readTimeout(20, TimeUnit.SECONDS);
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
                        .addHeader("User-Agent", "MoviePass/Android/20180301");
                Request request = requestBuilder.build();

                return chain.proceed(request);
            }
        });

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        sAuthenticatedInstance = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient.build())
                .build();
        sAuthenticatedAPI = sAuthenticatedInstance.create(Api.class);
    }

    public static void setupAuthenticatedGoWatchIt(Context context) {

        sAuthenticatedInstanceGoWatchIt = null;

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
                        .addHeader("x-api-key", "Lalq1yYxOx2d1tj2VlOHw8fFXXUnih3a8TIHInHU");
                Request request = requestBuilder.build();

                return chain.proceed(request);


            }
        });

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        sAuthenticatedInstanceGoWatchIt = new Retrofit.Builder()
                .baseUrl("https://click.moviepass.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient.build())
                .build();


        sAuthenticatedAPIGoWatchIt = sAuthenticatedInstanceGoWatchIt.create(Api.class);
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
        httpClient.connectTimeout(20, TimeUnit.SECONDS);
        httpClient.readTimeout(20, TimeUnit.SECONDS);
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
                        .addHeader("User-Agent", "MoviePass/Android/20170703");
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        sUnauthenticatedInstance = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();
        sUnauthenticatedAPI = sUnauthenticatedInstance.create(Api.class);
    }


    public static void setUpLocalStorage(Context context) {

        localStorageAPI = null;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        if (Constants.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.connectTimeout(20, TimeUnit.SECONDS);
        httpClient.readTimeout(20, TimeUnit.SECONDS);
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
                        .addHeader("User-Agent", "MoviePass/Android/20180301");
                Request request = requestBuilder.build();

                return chain.proceed(request);
            }
        });

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        localStorageInstance = new Retrofit.Builder()
                .baseUrl(a1URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient.build())
                .build();
        localStorageAPI = localStorageInstance.create(Api.class);
    }

    public static void setUpRegistration(Context context) {

        sAuthenticatedRegistrationAPI = null;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        if (Constants.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.connectTimeout(20, TimeUnit.SECONDS);
        httpClient.readTimeout(20, TimeUnit.SECONDS);
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
                        .addHeader("User-Agent", "MoviePass/Android/20180301");
                Request request = requestBuilder.build();

                return chain.proceed(request);
            }
        });

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        sAuthenticatedRegistrationInstance = new Retrofit.Builder()
                .baseUrl(registrationURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient.build())
                .build();
        sAuthenticatedRegistrationAPI = sAuthenticatedRegistrationInstance.create(Api.class);
    }
}
