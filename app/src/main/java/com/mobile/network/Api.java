package com.mobile.network;

import com.mobile.model.MoviePassCard;
import com.mobile.model.MoviesResponse;
import com.mobile.model.TheatersResponse;
import com.mobile.model.User;
import com.mobile.requests.AddressChangeRequest;
import com.mobile.requests.CancellationRequest;
import com.mobile.requests.CardActivationRequest;
import com.mobile.requests.ChangedMindRequest;
import com.mobile.requests.CheckInRequest;
import com.mobile.requests.CredentialsRequest;
import com.mobile.requests.CreditCardChangeRequest;
import com.mobile.requests.FacebookLinkRequest;
import com.mobile.requests.FacebookSignInRequest;
import com.mobile.requests.LogInRequest;
import com.mobile.requests.PerformanceInfoRequest;
import com.mobile.requests.PersonalInfoRequest;
import com.mobile.requests.SignUpRequest;
import com.mobile.requests.VerificationLostRequest;
import com.mobile.requests.VerificationRequest;
import com.mobile.responses.ActiveReservationResponse;
import com.mobile.responses.CancellationResponse;
import com.mobile.responses.CardActivationResponse;
import com.mobile.responses.ChangedMindResponse;
import com.mobile.responses.HistoryResponse;
import com.mobile.responses.PersonalInfoResponse;
import com.mobile.responses.RegistrationPlanResponse;
import com.mobile.responses.ReservationResponse;
import com.mobile.responses.RestrictionsResponse;
import com.mobile.responses.ScreeningsResponse;
import com.mobile.responses.SeatingsInfoResponse;
import com.mobile.responses.SignUpResponse;
import com.mobile.responses.UserInfoResponse;
import com.mobile.responses.VerificationLostResponse;
import com.mobile.responses.VerificationResponse;

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
    @POST("/rest/v1/session")
    Call<User> login(@Header(HEADER_UUID) String deviceId, @Body LogInRequest request);

    /* ForgotPassword */
    @GET("/rest/v1/users/passwordReset/{emailAddress}")
    Call<Object> forgotPassword(@Path("emailAddress") String email);

    /* FB Login */
    @POST("/rest/v1/auth/fb_login")
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
    @GET("/rest/v1/screenings")
    Call<ScreeningsResponse> getScreeningsForMovie(@Query("lat") double latitude, @Query("lon") double longitude, @Query("moviepassId") int moviepassId);

    /* Registration */
    @POST("/rest/v1/prospects")
    Call<Object> registerCredentials(@Body CredentialsRequest request);

    @GET("/rest/v1/register/amc_upgradeability/{zip}")
    Call<RegistrationPlanResponse> getPlans(@Path("zip") String zip);

    /* SignUp */
    @POST("/rest/v1/registration")
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

    //TODO
    /* History  */
    @GET("/rest/v1/reservations")
    Call<HistoryResponse> getReservations();

    /* Get Seats */
    @POST("/rest/v1/seats")
    Call<SeatingsInfoResponse> getSeats(@Query("tribuneTheaterId") int tribuneTheaterId, @Query("theater") String theater, @Body PerformanceInfoRequest request);

    /* Verify Ticket Photo */
    @POST("/rest/v1/reservations/{reservationId}/verification")
    Call<VerificationResponse> verifyTicket(@Path("reservationId") int reservationId, @Body VerificationRequest request);

    /* lost Ticket */
    @POST("/rest/v1/reservations/{reservationId}/verification")
    Call<VerificationLostResponse> lostTicket(@Path("reservationId") int reservationId, @Body VerificationLostRequest request);

    /* Theaters */
    @GET("/rest/v1/theaters/near")
    Call<TheatersResponse> getTheaters(@Query("lat") double latitude, @Query("lon") double longitude);

    /* Theater screenings (details) */
    @GET("/rest/v1/theaters/{id}/screenings")
    Call<ScreeningsResponse> getScreeningsForTheater(@Path("id") int id);

    /* User */
    @GET("/rest/v1/session")
    Call<RestrictionsResponse> getRestrictions();

    /* user Data */
    @GET("/rest/v1/users/{userId}")
    Call<UserInfoResponse> getUserData(@Path("userId") int userId);

    /* User Address */
    @PUT("/rest/v1/users/{userId}")
    Call<Object> updateAddress(@Path("userId") int userId, @Body AddressChangeRequest address);

    /* Billing Update */
    @PUT("/rest/v1/users/{userId}")
    Call<UserInfoResponse> updateBillingCard(@Path("userId") int userId, @Body CreditCardChangeRequest request);

    /* FB Link to */
    @POST("/rest/v1/users/link_to_facebook")
    Call<Object> linkToFacebook(@Body FacebookLinkRequest request);

    //TODO
    /* Cancel Subscription */
    @POST("/rest/v1/subscriptions/cancellation")
    Call<CancellationResponse> requestCancellation(@Body CancellationRequest request);
}
