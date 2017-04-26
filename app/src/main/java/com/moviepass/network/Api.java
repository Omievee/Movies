package com.moviepass.network;

import com.moviepass.model.MoviesResponse;
import com.moviepass.model.User;
import com.moviepass.requests.SignInRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Api {

    String HEADER_COOKIE = "Cookie";
    String HEADER_UUID = "device_uuid";
    String HEADER_UUIDD = "deviceUuid";

    /* Authentication */
    @POST("/api/v1/auth/login")
    Call<User> login(@Header(HEADER_UUID) String deviceId, @Body SignInRequest request  );

    /* Movies */
    @GET("/api/v3/movies")
    Call<MoviesResponse> getMovies(@Query("lat") double latitude, @Query("long") double longitude);

}
