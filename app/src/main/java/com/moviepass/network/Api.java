package com.moviepass.network;

import com.moviepass.model.MoviePassCard;
import com.moviepass.model.MoviesResponse;
import com.moviepass.model.TheatersResponse;
import com.moviepass.model.User;
import com.moviepass.requests.AddressChangeRequest;
import com.moviepass.requests.CancellationRequest;
import com.moviepass.requests.CardActivationRequest;
import com.moviepass.requests.ChangedMindRequest;
import com.moviepass.requests.CheckInRequest;
import com.moviepass.requests.CredentialsRequest;
import com.moviepass.requests.CreditCardChangeRequest;
import com.moviepass.requests.FacebookLinkRequest;
import com.moviepass.requests.FacebookSignInRequest;
import com.moviepass.requests.LogInRequest;
import com.moviepass.requests.PerformanceInfoRequest;
import com.moviepass.requests.PersonalInfoRequest;
import com.moviepass.requests.SignUpRequest;
import com.moviepass.requests.VerificationLostRequest;
import com.moviepass.requests.VerificationRequest;
import com.moviepass.responses.ActiveReservationResponse;
import com.moviepass.responses.CancellationResponse;
import com.moviepass.responses.CardActivationResponse;
import com.moviepass.responses.ChangedMindResponse;
import com.moviepass.responses.HistoryResponse;
import com.moviepass.responses.PersonalInfoResponse;
import com.moviepass.responses.RegistrationPlanResponse;
import com.moviepass.responses.ReservationResponse;
import com.moviepass.responses.RestrictionsResponse;
import com.moviepass.responses.ScreeningsResponse;
import com.moviepass.responses.SeatingsInfoResponse;
import com.moviepass.responses.SignUpResponse;
import com.moviepass.responses.UserInfoResponse;
import com.moviepass.responses.VerificationLostResponse;
import com.moviepass.responses.VerificationResponse;

import java.util.List;

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

    @POST("/api/v1/auth/fb_login")
    Call<User> loginWithFacebook(@Header(HEADER_UUID) String deviceId, @Body FacebookSignInRequest request);

    /* Cards */
    @GET("/api/v4/cards")
    Call<List<MoviePassCard>> getMoviePassCards();

    @POST("/api/v2/cards/activate")
    Call<CardActivationResponse> activateCard(@Body CardActivationRequest request);

    /* Movies */
    @GET("/api/v4/movies")
    Call<MoviesResponse> getMovies(@Query("lat") double latitude, @Query("long") double longitude);

    @GET("/api/v5/screenings")
    Call<ScreeningsResponse> getScreeningsForMovie(@Query("lat")
                                                           double latitude, @Query("lon") double longitude, @Query("moviepassId") int moviepassId);

    /* Registration */
    @POST("/register/create/json")
    Call<Object> registerCredentials(@Body CredentialsRequest request);

    @GET("/api/v2/register/amc_upgradeability/{zip}")
    Call<RegistrationPlanResponse> getPlans(@Path("zip") String zip);

    @POST("/register/create/json")
    Call<PersonalInfoResponse> registerPersonalInfo(@Body PersonalInfoRequest request);

    @POST("/api/v1/register/create/mobile")
    Call<SignUpResponse> signUp(@Header(HEADER_COOKIE) String session, @Body SignUpRequest request);

    /* Reservations */
    @POST("/api/v3/reservations")
    Call<ReservationResponse> checkIn(@Body CheckInRequest request);

    // TODO
    @GET("rest/v1/reservations/last")
    Call<ActiveReservationResponse> getLast(@Body ActiveReservationResponse activeReservation);



    @PUT("/api/v1/reservations")
    Call<ChangedMindResponse> changedMind(@Body ChangedMindRequest request);

    @POST("/api/v3/seats")
    Call<SeatingsInfoResponse> getSeats(@Query("tribuneTheaterId") int tribuneTheaterId, @Query("theater") String theater,
                                        @Body PerformanceInfoRequest request);

    @POST("/api/v1/reservations/{reservationId}/verification")
    Call<VerificationResponse> verifyTicket(@Path("reservationId") int reservationId, @Body VerificationRequest request);

    @POST("/api/v1/reservations/{reservationId}/verification")
    Call<VerificationLostResponse> lostTicket(@Path("reservationId") int reservationId, @Body VerificationLostRequest request);

    @GET("/api/v2/reservations")
    Call<HistoryResponse> getReservations();

    /* Theaters */
    @GET("/api/v3/theaters/near")
    Call<TheatersResponse> getTheaters(@Query("lat") double latitude, @Query("lon") double longitude);

    @GET("/api/v5/theaters/{id}/screenings")
    Call<ScreeningsResponse> getScreeningsForTheater(@Path("id") int id);


    /* User */
    @GET("/api/v2/auth/restrictions")
    Call<RestrictionsResponse> getRestrictions();

    @GET("/api/v1/users/{userId}")
    Call<UserInfoResponse> getUserData(@Path("userId") int userId);

    @PUT("/api/v1/users/{userId}")
    Call<Object> updateAddress(@Path("userId") int userId, @Body AddressChangeRequest address);

    @PUT("/api/v1/users/{userId}")
    Call<UserInfoResponse> updateBillingCard(@Path("userId") int userId, @Body CreditCardChangeRequest request);

    @POST("/api/v1/users/link_to_facebook")
    Call<Object> linkToFacebook(@Body FacebookLinkRequest request);

    @POST("/api/v1/subscriptions/cancellation")
    Call<CancellationResponse> requestCancellation(@Body CancellationRequest request);
}
