package com.mobile.network;

import com.mobile.billing.BillingInfo;
import com.mobile.history.response.ReservationHistoryResponse;
import com.mobile.loyalty.TheaterChain;
import com.mobile.model.Emails;
import com.mobile.model.ProviderInfo;
import com.mobile.model.User;
import com.mobile.requests.AddressChangeRequest;
import com.mobile.requests.CancellationRequest;
import com.mobile.requests.CardActivationRequest;
import com.mobile.requests.ChangeEmailRequest;
import com.mobile.requests.ChangePasswordRequest;
import com.mobile.requests.ChangedMindRequest;
import com.mobile.requests.CredentialsRequest;
import com.mobile.requests.CreditCardChangeRequest;
import com.mobile.requests.FacebookSignInRequest;
import com.mobile.requests.LogInRequest;
import com.mobile.requests.SignUpRequest;
import com.mobile.requests.TicketInfoRequest;
import com.mobile.requests.VerificationLostRequest;
import com.mobile.requests.VerificationRequest;
import com.mobile.reservation.CurrentReservationV2;
import com.mobile.responses.AllMoviesResponse;
import com.mobile.responses.AndroidIDVerificationResponse;
import com.mobile.responses.CancellationResponse;
import com.mobile.responses.CardActivationResponse;
import com.mobile.responses.ChangeEmailResponse;
import com.mobile.responses.ChangePasswordResponse;
import com.mobile.responses.ChangedMindResponse;
import com.mobile.responses.HistoryResponse;
import com.mobile.responses.LocalStorageMovies;
import com.mobile.responses.TheatersResponse;
import com.mobile.responses.MicroServiceRestrictionsResponse;
import com.mobile.responses.PlanResponse;
import com.mobile.responses.ReferAFriendResponse;
import com.mobile.responses.ReservationResponse;
import com.mobile.responses.ScreeningsResponseV2;
import com.mobile.responses.SeatingsInfoResponse;
import com.mobile.responses.SignUpResponse;
import com.mobile.responses.UserInfoResponse;
import com.mobile.responses.VerificationLostResponse;
import com.mobile.responses.VerificationResponse;

import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

import static com.mobile.Constants.USER_ID;

public interface Api {

    String HEADER_COOKIE = "Cookie";
    String HEADER_UUID = "device_uuid";

    /* LogIn */
    @POST("/rest/v1/session")
    Call<User> login(@Header(HEADER_UUID) String deviceId, @Body LogInRequest request);

    /* ForgotPassword */
    @GET("/rest/v1/password_reset/{emailAddress}")
    Call<Object> forgotPassword(@Path("emailAddress") String email);

    /* FB Login */
    @POST("/rest/v1/auth/fb_login")
    Call<User> loginWithFacebook(@Header(HEADER_UUID) String deviceId, @Body FacebookSignInRequest request);

    /**
     * Change Password
     */
    @POST("rest/v1/passwordChange")
    Call<ChangePasswordResponse> changePassword(@Body ChangePasswordRequest request);

    /**
     * Change Email
     */
    @POST("rest/v1/emailChange")
    Call<ChangeEmailResponse> changeEmail(@Body ChangeEmailRequest request);

    /* Activate MP Card */
    @POST("/rest/v1/cards/activate")
    Call<CardActivationResponse> activateCard(@Body CardActivationRequest request);

    /* Activate MP Card */
    @POST("/rest/v1/cards/activate")
    Single<CardActivationResponse> activateCardRX(@Body CardActivationRequest request);


    @GET("/rest/v2/screenings")
    Single<ScreeningsResponseV2> getScreeningsForMovieRx(@Query("lat") double latitude, @Query("lon") double longitude, @Query("moviepassId") int moviepassId);

    /* Registration */
    @POST("mobile/check/email")
    Call<Object> registerCredentials(@Body CredentialsRequest request);

    @GET("/register/plans")
    Call<PlanResponse> getPlans();

    /* SignUp */
    @POST("mobile/register")
    Call<SignUpResponse> signUp(@Header(HEADER_COOKIE) String session, @Body SignUpRequest request);


    @POST("/rest/v3/reservations/peak-check")
    Single<SurgeResponse> surgeCheck(@Body ProviderInfo info);

    @POST("/rest/v2/reservations")
    Single<ReservationResponse> reserve(@Body TicketInfoRequest request);

    @GET("rest/v2/reservations/last")
    Single<CurrentReservationV2> lastReservation();

    /* Cancel Reservation  */
    @PUT("/rest/v1/reservations")
    Single<ChangedMindResponse> changedMind(@Body ChangedMindRequest request);

    /* History  */
    @GET("/rest/v1/reservations/history")
    Call<HistoryResponse> getReservations();

    @GET("/rest/v1/reservations/history")
    Single<ReservationHistoryResponse> getReservationHistory();

    @GET("/rest/v2/seats")
    Single<SeatingsInfoResponse> getSeats(@Query("tribuneTheaterId") int tribuneTheaterId, @Query("theater") String theater, @Query("performanceId") String performanceId);

    /* Verify Ticket Photo */
    @POST("/rest/v1/reservations/{reservationId}/verification")
    Call<VerificationResponse> verifyTicket(@Path("reservationId") int reservationId, @Body VerificationRequest request);

    /* Verify Ticket Photo */
    @POST("/rest/v1/reservations/{reservationId}/verification")
    Single<VerificationResponse> verifyTicketV2(@Path("reservationId") int reservationId, @Body VerificationRequest request);

    /* lost Ticket */
    @POST("/rest/v1/reservations/{reservationId}/verification")
    Call<VerificationLostResponse> lostTicket(@Path("reservationId") int reservationId, @Body VerificationLostRequest request);

    /* Theater screenings (details) */
    @GET("/rest/v2/theater/{id}/screenings")
    Single<ScreeningsResponseV2> getScreeningsForTheaterV2(@Path("id") int id);

    /* user Data */
    @GET("/rest/v1/users/{userId}")
    Call<UserInfoResponse> getUserData(@Path("userId") int userId);

    /* user Data */
    @GET("/rest/v1/users/{userId}")
    Single<UserInfoResponse> getUserDataRx(@Path("userId") int userId);

    @POST("/rest/v1/users/exists")
    Single<Emails> usersExist(@Body Emails emails);

    /* User Address */
    @PUT("/rest/v1/users/{userId}")
    Call<Object> updateAddress(@Path("userId") int userId, @Body AddressChangeRequest address);

    /* Billing Update */
    @PUT("/rest/v1/users/{userId}")
    Call<UserInfoResponse> updateBillingCard(@Path("userId") int userId, @Body CreditCardChangeRequest request);

    @PUT("/rest/v2/users/{userId}")
    Single<ResponseBody> updateBilling(@Path("userId") int userId, @Body BillingInfo info);

    /* Cancel Subscription */
    @POST("/rest/v1/subscription/cancellation")
    Single<CancellationResponse> requestCancellation(@Body CancellationRequest request);

    /*ALL MOVIES FOR MAIN PAGE */
    @GET("/#env#/movies/current.json")
    Call<LocalStorageMovies> getAllCurrentMovies();


    /* ALL MOVIES FOR SEARCH */
    @GET("/#env#/movies/all.json")
    Call<List<AllMoviesResponse>> getAllMovies();


    /* ALL THEATERS */
    @GET("/theaters/all.json")
    Call<TheatersResponse> getAllMoviePassTheaters();


    //NEW RESTRICTIONS
    @GET("auth/v1/session/{userId}")
    Call<MicroServiceRestrictionsResponse> getInterstitialAlert(@Path("userId") int userId);

    @POST("/rest/v1/movies/{movieId}/rate")
    Single<HistoryResponse> submitRatingRx(@Path("movieId") int movieId, @Body HistoryResponse request);


    //REFER A FRIEND
    @GET("/rest/v1/sharing/messages")
    Call<ReferAFriendResponse> referAFriend();

    //Device ID  Verification
    @POST("/rest/v1/device/verification")
    Call<AndroidIDVerificationResponse> verifyAndroidID(@Header(USER_ID) String user_id, @Body AndroidIDVerificationResponse request);

    @POST("/rest/v1/device/verification")
    Single<AndroidIDVerificationResponse> verifyAndroidIDRx(@Header(USER_ID) String user_id, @Body AndroidIDVerificationResponse request);

    @GET("/rest/v1/loyalty/list")
    Single<List<TheaterChain>> theaterChains();

    @POST("/rest/v1/loyalty/{chain}/signIn")
    Single<Map<String, Object>> theaterChainSignIn(@Path("chain") String chain, @Body Map<String, String> chainData);
}
