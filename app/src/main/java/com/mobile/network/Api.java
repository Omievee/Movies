package com.mobile.network;

import com.google.gson.JsonObject;
import com.mobile.loyalty.TheaterChain;
import com.mobile.model.Emails;
import com.mobile.model.ProviderInfo;
import com.mobile.model.User;
import com.mobile.plans.ChangePlanResponse;
import com.mobile.plans.UpdatePlan;
import com.mobile.plans.UpdatePlanResponse;
import com.mobile.requests.AddressChangeRequest;
import com.mobile.requests.CardActivationRequest;
import com.mobile.requests.ChangeEmailRequest;
import com.mobile.requests.ChangePasswordRequest;
import com.mobile.requests.ChangedMindRequest;
import com.mobile.requests.CredentialsRequest;
import com.mobile.requests.FacebookSignInRequest;
import com.mobile.requests.LogInRequest;
import com.mobile.requests.SignUpRequest;
import com.mobile.requests.TicketInfoRequest;
import com.mobile.requests.VerificationLostRequest;
import com.mobile.requests.VerificationRequest;
import com.mobile.reservation.CurrentReservationV2;
import com.mobile.responses.AndroidIDVerificationResponse;
import com.mobile.responses.CardActivationResponse;
import com.mobile.responses.ChangeEmailResponse;
import com.mobile.responses.ChangePasswordResponse;
import com.mobile.responses.ChangedMindResponse;
import com.mobile.responses.PlanResponse;
import com.mobile.responses.ReferAFriendResponse;
import com.mobile.responses.ReservationResponse;
import com.mobile.responses.RestrictionsResponse;
import com.mobile.responses.ScreeningsResponseV2;
import com.mobile.responses.SeatingsInfoResponse;
import com.mobile.responses.SignUpResponse;
import com.mobile.responses.UserInfoResponse;
import com.mobile.responses.VerificationLostResponse;
import com.mobile.responses.VerificationResponse;

import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
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
    Single<ChangePasswordResponse> changePassword(@Body ChangePasswordRequest request);

    /*Change Email*/
    @POST("rest/v1/emailChange")
    Single<ChangeEmailResponse> changeEmail(@Body ChangeEmailRequest request);

    /* Activate MP Card */
    @POST("/rest/v1/cards/activate")
    Single<CardActivationResponse> activateCardRX(@Body CardActivationRequest request);


    @GET("/rest/v3/screenings/{segment}")
    Single<ScreeningsResponseV2> getScreeningsForMovieRx(@Path("segment") int segment, @Query("lat") double latitude, @Query("lon") double longitude, @Query("moviepassId") int moviepassId);

    /* Registration */
    @POST("mobile/check/email")
    Call<Object> registerCredentials(@Body CredentialsRequest request);

    @GET("/register/plan")
    Call<PlanResponse> getPlans();

    /* SignUp */
    @POST("mobile/register")
    Call<SignUpResponse> signUp(@Header(HEADER_COOKIE) String session, @Body SignUpRequest request);


    @POST("/rest/v2/reservations/restrictions-check")
    Single<RestrictionsCheckResponse> restrictionsCheck(@Body ProviderInfo info);

    @POST("/rest/v2/reservations")
    Single<ReservationResponse> reserve(@Body TicketInfoRequest request);

    @GET("rest/v2/reservations/last")
    Single<CurrentReservationV2> lastReservation();

    /* Cancel Reservation  */
    @PUT("/rest/v1/reservations")
    Single<ChangedMindResponse> changedMind(@Body ChangedMindRequest request);


    @GET("/rest/v2/seats")
    Single<SeatingsInfoResponse> getSeats(@Query("tribuneTheaterId") int tribuneTheaterId, @Query("theater") String theater, @Query("performanceId") String performanceId);


    /* Verify Ticket Photo */
    @POST("/rest/v1/reservations/{reservationId}/verification")
    Single<VerificationResponse> verifyTicketV2(@Path("reservationId") int reservationId, @Body VerificationRequest request);

    /* lost Ticket */
    @POST("/rest/v1/reservations/{reservationId}/verification")
    Call<VerificationLostResponse> lostTicket(@Path("reservationId") int reservationId, @Body VerificationLostRequest request);

    /* Theater screenings (details) */
    @GET("/rest/v3/theater/{id}/screenings/{segment}")
    Single<ScreeningsResponseV2> getScreeningsForTheaterV2(@Path("id") int id, @Path("segment") int segment);


    /* user SubscriptionData */
    @GET("/rest/v1/users/{userId}")
    Single<UserInfoResponse> getUserDataRx(@Path("userId") int userId);

    @POST("/rest/v1/users/exists")
    Single<Emails> usersExist(@Body Emails emails);

    /* User Address */
    @PUT("/rest/v1/users/{userId}")
    Single<Object> updateAddress(@Path("userId") int userId, @Body AddressChangeRequest address);

    //NEW RESTRICTIONS
    @GET("auth/v1/session/{userId}")
    Call<RestrictionsResponse> getInterstitialAlert(@Path("userId") int userId);

    @GET("/rest/v1/sharing/messages")
    Single<ReferAFriendResponse> referAFriend();

    //Device ID  Verification
    @POST("/rest/v1/device/verification")
    Call<AndroidIDVerificationResponse> verifyAndroidID(@Header(USER_ID) String user_id, @Body AndroidIDVerificationResponse request);

    @POST("/rest/v1/device/verification")
    Single<AndroidIDVerificationResponse> verifyAndroidIDRx(@Header(USER_ID) String user_id, @Body AndroidIDVerificationResponse request);

    @GET("/rest/v1/loyalty/list")
    Single<List<TheaterChain>> theaterChains();

    @POST("/rest/v1/loyalty/{chain}/signIn")
    Single<Map<String, Object>> theaterChainSignIn(@Path("chain") String chain, @Body Map<String, String> chainData);

    @HTTP(method = "DELETE", path = "/rest/v1/loyalty/{chain}/remove", hasBody = true)
    Single<Map<String, Object>> theaterChainRemove(@Path("chain") String chain, @Body Map<String, String> chainData);

    @GET("/gw/subscriptions/v1/available-plans")
    Single<ChangePlanResponse> getAvailablePlans();

    @POST("/gw/subscriptions/v1/change-plan/user")
    Single<UpdatePlanResponse> updateCurrentPlan(@Body UpdatePlan newPlanId);
}