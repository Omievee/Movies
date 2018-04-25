package com.mobile.network;

import com.mobile.model.MoviePassCard;
import com.mobile.model.MoviesResponse;
import com.mobile.model.TheatersResponse;
import com.mobile.model.User;
import com.mobile.requests.AddressChangeRequest;
import com.mobile.requests.CancellationRequest;
import com.mobile.requests.CardActivationRequest;
import com.mobile.requests.ChangePasswordRequest;
import com.mobile.requests.ChangedMindRequest;
import com.mobile.requests.CheckInRequest;
import com.mobile.requests.CredentialsRequest;
import com.mobile.requests.CreditCardChangeRequest;
import com.mobile.requests.FacebookLinkRequest;
import com.mobile.requests.FacebookSignInRequest;
import com.mobile.requests.LogInRequest;
import com.mobile.requests.PerformanceInfoRequest;
import com.mobile.requests.SignUpRequest;
import com.mobile.requests.VerificationLostRequest;
import com.mobile.requests.VerificationRequest;
import com.mobile.responses.ActiveReservationResponse;
import com.mobile.responses.AllMoviesResponse;
import com.mobile.responses.AndroidIDVerificationResponse;
import com.mobile.responses.CancellationResponse;
import com.mobile.responses.CardActivationResponse;
import com.mobile.responses.ChangePasswordResponse;
import com.mobile.responses.ChangedMindResponse;
import com.mobile.responses.GoWatchItResponse;
import com.mobile.responses.HistoryResponse;
import com.mobile.responses.LocalStorageMovies;
import com.mobile.responses.LocalStorageTheaters;
import com.mobile.responses.MicroServiceRestrictionsResponse;
import com.mobile.responses.PlanResponse;
import com.mobile.responses.ReferAFriendResponse;
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
    String FLAG = "flag";
    String HEADER_UUID = "device_uuid";
    String HEADER_UUIDD = "deviceAndroidID";
    String HEADER_GOWATCHIT = "x-api-key";


    /* LogIn */
    @POST("/rest/v1/session")
    Call<User> login(@Header(FLAG) String flag, @Body LogInRequest request);

//
//    /* LogIn */
//    @POST("/api/v1/auth/login")
//    Call<User> login(@Header(HEADER_UUID) String deviceId, @Body LogInRequest request);

    /* ForgotPassword */
    @GET("/rest/v1/password_reset/{emailAddress}")
    Call<Object> forgotPassword(@Path("emailAddress") String email);

    /* FB Login */
    @POST("/rest/v1/auth/fb_login")
    Call<User> loginWithFacebook(@Header(HEADER_UUID) String deviceId, @Body FacebookSignInRequest request);

    /* Get Cards? */
    @GET("/rest/v1/cards")
    Call<List<MoviePassCard>> getMoviePassCards();

    /**
     * Change Password
     */
    @POST("rest/v1/passwordChange")
    Call<ChangePasswordResponse> changePassword(@Body ChangePasswordRequest request);

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
    @POST("mobile/check/email")
    Call<Object> registerCredentials(@Body CredentialsRequest request);

    @GET("/register/plans")
    Call<PlanResponse> getPlans();

    /* SignUp */
    @POST("mobile/register")
    Call<SignUpResponse> signUp(@Header(HEADER_COOKIE) String session, @Body SignUpRequest request);

    /* Check In */
    @POST("/rest/v1/reservations")
    Call<ReservationResponse> checkIn(@Body CheckInRequest request);


    /* GET PENDING RESERVATION */
    @GET("rest/v1/reservations/last")
    Call<ActiveReservationResponse> last();

    /* Cancel Reservation  */
    @PUT("/rest/v1/reservations")
    Call<ChangedMindResponse> changedMind(@Body ChangedMindRequest request);

    /* History  */
    @GET("/rest/v1/reservations/history")
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

    /**
     * User
     */
    @GET("/rest/v1/session/{userId}")
    Call<RestrictionsResponse> getRestrictions( @Path("userId") int userId);


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

    /* Cancel Subscription */
    @POST("/rest/v1/subscription/cancellation")
    Call<CancellationResponse> requestCancellation(@Body CancellationRequest request);

    /* Open App Go Watch It Event */
    @GET("/prod/ingest")
    Call<GoWatchItResponse> openAppEvent(@Query("ct") String ct, @Query("ci") String ci, @Query("cd") String cd,
                                         @Query("e") String e, @Query("c") String campaign, @Query("m") String m, @Query("mc") String mc,
                                         @Query("u") String u, @Query("o") String o, @Query("l") String l,
                                         @Query("ln") String ln, @Query("eid[movie_pass]") String movie_pass, @Query("eid[aaid]") String idfa,
                                         @Query("ab") String ab, @Query("av") String av, @Query("lts") String lts);

    @GET("/prod/ingest")
    Call<GoWatchItResponse> clickOnShowtime(@Query("e") String engagement, @Query("et") String et, @Query("tht") String tht,
                                            @Query("thd") String thd, @Query("tn") String th, @Query("thc") String thc,
                                            @Query("thr") String thr, @Query("thz") String thz, @Query("tha") String tha,
                                            @Query("ct") String ct, @Query("ci") String ci, @Query("cd") String cd,
                                            @Query("c") String campaign, @Query("m") String m, @Query("mc") String mc,
                                            @Query("u") String u, @Query("o") String o, @Query("l") String l,
                                            @Query("ln") String ln, @Query("eid[movie_pass]") String movie_pass, @Query("eid[aaid]") String idfa,
                                            @Query("ab") String ab, @Query("av") String av, @Query("lts") String lts);

    @GET("/prod/ingest")
    Call<GoWatchItResponse> ticketPurchase(@Query("e") String engagement, @Query("tht") String tht,
                                           @Query("thd") String thd, @Query("tn") String th, @Query("thc") String thc,
                                           @Query("thr") String thr, @Query("thz") String thz, @Query("tha") String tha,
                                           @Query("ct") String ct, @Query("ci") String ci, @Query("cd") String cd,
                                           @Query("c") String campaign, @Query("m") String m, @Query("mc") String mc,
                                           @Query("u") String u, @Query("o") String o, @Query("l") String l,
                                           @Query("ln") String ln, @Query("eid[movie_pass]") String movie_pass, @Query("eid[aaid]") String idfa,
                                           @Query("ab") String ab, @Query("av") String av, @Query("lts") String lts);

    @GET("/prod/ingest")
    Call<GoWatchItResponse> searchTheatersMovies(@Query("e") String engagement,
                                                 @Query("ct") String ct, @Query("ci") String ci,
                                                 @Query("tr") String tr,
                                                 @Query("c") String campaign, @Query("m") String m, @Query("mc") String mc,
                                                 @Query("u") String u, @Query("o") String o, @Query("l") String l,
                                                 @Query("ln") String ln, @Query("eid[movie_pass]") String movie_pass, @Query("eid[aaid]") String idfa,
                                                 @Query("ab") String ab, @Query("av") String av, @Query("lts") String lts);

    @GET("/prod/ingest")
    Call<GoWatchItResponse> openTheaterEvent(@Query("e") String engagement,
                                             @Query("tn") String th, @Query("thc") String thc,
                                             @Query("thr") String thr, @Query("thz") String thz, @Query("tha") String tha,
                                             @Query("ct") String ct, @Query("ci") String ci,
                                             @Query("c") String campaign, @Query("m") String m, @Query("mc") String mc,
                                             @Query("u") String u, @Query("o") String o, @Query("l") String l,
                                             @Query("ln") String ln, @Query("eid[movie_pass]") String movie_pass, @Query("eid[aaid]") String idfa,
                                             @Query("ab") String ab, @Query("av") String av, @Query("lts") String lts);

    @GET("/prod/ingest")
    Call<GoWatchItResponse> openMapEvent(@Query("e") String engagement,
                                         @Query("ct") String ct, @Query("ci") String ci,
                                         @Query("et") String et,
                                         @Query("c") String campaign, @Query("m") String m, @Query("mc") String mc,
                                         @Query("u") String u, @Query("o") String o, @Query("l") String l,
                                         @Query("ln") String ln, @Query("eid[movie_pass]") String movie_pass, @Query("eid[aaid]") String idfa,
                                         @Query("ab") String ab, @Query("av") String av, @Query("lts") String lts);


    /*ALL MOVIES FOR MAIN PAGE */
    @GET("/staging/movies/current.json")
    Call<LocalStorageMovies> getAllCurrentMovies();


    /* ALL MOVIES FOR SEARCH */
    @GET("/staging/movies/all.json")
    Call<List<AllMoviesResponse>> getAllMovies();


    /* ALL THEATERS */
    @GET("/theaters/all.json")
    Call<LocalStorageTheaters> getAllMoviePassTheaters();


    //NEW RESTRICTIONS
    @GET("auth/v1/session/{userId}")
    Call<MicroServiceRestrictionsResponse> getInterstitialAlert( @Path("userId") int userId);

    @POST("/rest/v1/movies/{movieId}/rate")
    Call<HistoryResponse> submitRating(@Path("movieId") int movieId, @Body HistoryResponse request);


    //REFER A FRIEND
    @GET("/rest/v1/sharing/messages")
    Call<ReferAFriendResponse> referAFriend();

    //Device ID  Verification
    @POST(" /api/v1/device/verification")
    Call<AndroidIDVerificationResponse> verifyAndroidID(@Body AndroidIDVerificationResponse request);
}
