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

    /* LogIn */
    @POST("/api/v1/auth/login")
    Call<User> login(@Header(HEADER_UUID) String deviceId, @Body LogInRequest request);

    /* ForgotPassword */
    @GET("/rest/v1/auth/passwordReset/{emailAddress}")
    Call<Object> forgotPassword(@Path("emailAddress") String email);

    /* FB Login */
    @POST("/api/v1/auth/fb_login")
    Call<User> loginWithFacebook(@Header(HEADER_UUID) String deviceId, @Body FacebookSignInRequest request);

    /* Get Cards? */
    @GET("/rest/v1/cards")
    Call<List<MoviePassCard>> getMoviePassCards();

    /* Activate MP Card */
    @POST("/rest/v1/cards/activate")
    Call<CardActivationResponse> activateCard(@Body CardActivationRequest request);

    /* Movies */
    @GET("/rest/v1/movies")
    Call<MoviesResponse> getMovies(@Query("lat") double latitude, @Query("long") double longitude);

    /* Screenings for Movies (details) */
    @GET("/api/v5/screenings")
    Call<ScreeningsResponse> getScreeningsForMovie(@Query("lat") double latitude, @Query("lon") double longitude, @Query("moviepassId") int moviepassId);

    /* Registration */
    @POST("/register/create/json")
    Call<Object> registerCredentials(@Body CredentialsRequest request);

    @GET("/api/v2/register/amc_upgradeability/{zip}")
    Call<RegistrationPlanResponse> getPlans(@Path("zip") String zip);

    /* Personal Info */
    @POST("/register/create/json")
    Call<PersonalInfoResponse> registerPersonalInfo(@Body PersonalInfoRequest request);

    /* SignUp */
    @POST("/rest/v1/register/create/mobile")
    Call<SignUpResponse> signUp(@Header(HEADER_COOKIE) String session, @Body SignUpRequest request);

    /* Check In */
    @POST("/rest/v1/reservations")
    Call<ReservationResponse> checkIn(@Body CheckInRequest request);


    /* GET PENDING RESERVATION */
    @GET("rest/v1/reservations/last")
    Call<ActiveReservationResponse> getLast();

    /* Cancel Reservation  */
    @PUT("/rest/v1/reservations")
    Call<ChangedMindResponse> changedMind(@Body ChangedMindRequest request);

    /* History  */
    @GET("/api/v2/reservations")
    Call<HistoryResponse> getReservations();

    /* Get Seats */
    @POST("/rest/v1/seats")
    Call<SeatingsInfoResponse> getSeats(@Query("tribuneTheaterId") int tribuneTheaterId, @Query("theater") String theater, @Body PerformanceInfoRequest request);

    /* Verify Ticket Photo */
    @POST("/rest/v1/reservations/{reservationId}/verification")
    Call<VerificationResponse> verifyTicket(@Path("reservationId") int reservationId, @Body VerificationRequest request);

    /* lost Ticket */
    @POST("/api/v1/reservations/{reservationId}/verification")
    Call<VerificationLostResponse> lostTicket(@Path("reservationId") int reservationId, @Body VerificationLostRequest request);

    /* Theaters */
    @GET("/api/v3/theaters/near")
    Call<TheatersResponse> getTheaters(@Query("lat") double latitude, @Query("lon") double longitude);

    /* Theater screenings (details) */
    @GET("/api/v5/theaters/{id}/screenings")
    Call<ScreeningsResponse> getScreeningsForTheater(@Path("id") int id);

    /* User */
    @GET("/api/v2/auth/restrictions")
    Call<RestrictionsResponse> getRestrictions();

    /* user Data */
    @GET("/rest/v1/users/{userId}")
    Call<UserInfoResponse> getUserData(@Path("userId") int userId);

    /* User Address */
    @PUT("/api/v1/users/{userId}")
    Call<Object> updateAddress(@Path("userId") int userId, @Body AddressChangeRequest address);

    /* Billing Update */
    @PUT("/rest/v1/users/{userId}")
    Call<UserInfoResponse> updateBillingCard(@Path("userId") int userId, @Body CreditCardChangeRequest request);

    /* FB Link to */
    @POST("/api/v1/users/link_to_facebook")
    Call<Object> linkToFacebook(@Body FacebookLinkRequest request);

    /* Cancel Subscription */
    @POST("/rest/v1/subscriptions/cancellation")
    Call<CancellationResponse> requestCancellation(@Body CancellationRequest request);
}
