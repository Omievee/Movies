package com.moviepass.network;

import com.moviepass.model.MoviesResponse;
import com.moviepass.model.Plan;
import com.moviepass.model.TheatersResponse;
import com.moviepass.model.User;
import com.moviepass.requests.ChangedMindRequest;
import com.moviepass.requests.CheckInRequest;
import com.moviepass.requests.LogInRequest;
import com.moviepass.requests.PerformanceInfoRequest;
import com.moviepass.responses.ChangedMindResponse;
import com.moviepass.responses.PlanResponse;
import com.moviepass.responses.ReservationResponse;
import com.moviepass.responses.ScreeningsResponse;
import com.moviepass.responses.SeatingsInfoResponse;

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
    Call<User> login(@Header(HEADER_UUID) String deviceId, @Body LogInRequest request);

    @GET("/api/v1/auth/passwordReset/{emailAddress}")
    Call<Object> forgotPassword(@Path("emailAddress") String email);

    /* Movies */
    @GET("/api/v4/movies")
    Call<MoviesResponse> getMovies(@Query("lat") double latitude, @Query("long") double longitude);

    @GET("/api/v5/screenings")
    Call<ScreeningsResponse> getScreeningsForMovie(@Query("lat")
            double latitude, @Query("lon") double longitude, @Query("moviepassId") int moviepassId);

    /* Registration */
    @GET("/api/v2/register/amc_upgradeability/{zip}")
    Call<Plan> getPlans(@Path("zip") String zip);


    /* Reservations */
    @POST("/api/v3/reservations")
    Call<ReservationResponse> checkIn(@Body CheckInRequest request);

    @PUT("/api/v1/reservations")
    Call<ChangedMindResponse> changedMind(@Body ChangedMindRequest request);

    @POST("/api/v3/seats")
    Call<SeatingsInfoResponse> getSeats(@Query("tribuneTheaterId") int tribuneTheaterId, @Query("theater") String theater,
                                        @Body PerformanceInfoRequest request);


    /* Theaters */
    @GET("/api/v3/theaters/near")
    Call<TheatersResponse> getTheaters(@Query("lat") double latitude, @Query("lon") double longitude);

    @GET("/api/v5/theaters/{id}/screenings")
    Call<ScreeningsResponse> getScreeningsForTheater(@Path("id") int id);

}
