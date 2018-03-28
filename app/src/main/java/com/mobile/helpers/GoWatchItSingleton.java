package com.mobile.helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mobile.UserPreferences;
import com.mobile.activities.MoviesActivity;
import com.mobile.model.ProspectUser;
import com.mobile.model.Screening;
import com.mobile.model.Theater;
import com.mobile.network.RestCallback;
import com.mobile.network.RestClient;
import com.mobile.network.RestError;
import com.mobile.responses.GoWatchItResponse;
import com.moviepass.BuildConfig;

import java.security.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by ivonneortega on 3/11/18.
 */

public class GoWatchItSingleton {

    private static GoWatchItSingleton instance;
    private String campaign;
    private String debug = "false";
    private String l = "0.0";
    private String ln = "0.0";
    private String IDFA;
//    String l = String.valueOf(UserPreferences.getLatitude());
//    String ln = String.valueOf(UserPreferences.getLongitude());

    private GoWatchItSingleton() {
        campaign = "no_campaign";
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(String campaign) {
        if(campaign!=null)
            this.campaign = campaign;
    }

    public static GoWatchItSingleton getInstance() {

        synchronized (ContextSingleton.class) {
            if (instance == null) {
                instance = new GoWatchItSingleton();
            }
            return instance;
        }
    }

    private String currentTimeStamp(){
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        return ts;
    }

    public void userOpenedApp(Context context, String deepLink){

        String userId = String.valueOf(UserPreferences.getUserId());
        if (deepLink == null)
            deepLink = "https://www.moviepass.com/go";
        String thisCampaign = GoWatchItSingleton.getInstance().getCampaign();

        String versionName = BuildConfig.VERSION_NAME;
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        String lts = currentTimeStamp();
        IDFA = UserPreferences.getAAID();

        RestClient.getAuthenticatedAPIGoWatchIt().openAppEvent("Unset",
                "-1", "app_open", thisCampaign, "app", "android", deepLink, "organic",
                l, ln, userId, IDFA, versionCode, versionName, lts).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
                if (response != null && response.isSuccessful()) {
                    GoWatchItResponse responseBody = response.body();
                    Log.d("GO WATCH IT APP OPEN", "onResponse: " + responseBody.getMessage());
                }


            }

            @Override
            public void failure(RestError restError) {
                Toast.makeText(context, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void userOpenedMovie(String movieId, String url) {


        String userId = String.valueOf(UserPreferences.getUserId());

        String versionName = BuildConfig.VERSION_NAME;
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        String campaign = GoWatchItSingleton.getInstance().getCampaign();
        String lts = currentTimeStamp();
        IDFA = UserPreferences.getAAID();


        RestClient.getAuthenticatedAPIGoWatchIt().openAppEvent("Movie",
                String.valueOf(movieId), "impression", campaign, "app", "android", url, "organic",
                l, ln, userId, IDFA, versionCode, versionName, lts).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {

//                progress.setVisibility(View.GONE);

                if (response != null && response.isSuccessful()) {
                    GoWatchItResponse responseBody = response.body();
                    Log.d("GO WATCH IT MOVIE", "onResponse: " + responseBody.getMessage());
                }


            }

            @Override
            public void failure(RestError restError) {
//                progress.setVisibility(View.GONE);
                // Toast.makeText(MovieActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void userClickedOnShowtime(Theater theater, Screening screening, String showtime, String movieId, String url) {

        String userId = String.valueOf(UserPreferences.getUserId());
        IDFA = UserPreferences.getAAID();

        String versionName = BuildConfig.VERSION_NAME;
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        String tht, thd, tn, thc, thr, thz, tha;
        tht = showtime.trim();
        tn = screening.getTheaterName();
        thc = theater.getCity();
        thr = theater.getState();
        thz = theater.getZip();
        tha = theater.getAddress();
        String lts = currentTimeStamp();

        String result = "";
        thd = "";
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
        try {
            Date date = format1.parse(screening.getDate());
            SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            result = format2.format(date);
            thd = result;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        RestClient.getAuthenticatedAPIGoWatchIt().clickOnShowtime("engagement", "theater_click", tht, thd, tn, thc, thr, thz, tha, "Movie",
                movieId, campaign, "app", "android", url, "organic",
                l, ln, userId, IDFA, versionCode, versionName, lts).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
                if (response != null && response.isSuccessful()) {
                    GoWatchItResponse responseBody = response.body();
                    Log.d("GO WATCH IT SHOWTIME", "onResponse: " + responseBody.getMessage());
                }


            }

            @Override
            public void failure(RestError restError) {
//                progress.setVisibility(View.GONE);
                // Toast.makeText(MovieActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void checkInEvent(Theater theater, Screening screening, String showtime, String engagement, String movieId, String url) {

        String userId = String.valueOf(UserPreferences.getUserId());
        IDFA = UserPreferences.getAAID();

        String versionName = BuildConfig.VERSION_NAME;
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        String tht, thd, tn, thc, thr, thz, tha;
        tht = showtime.trim();
        tn = screening.getTheaterName();
        thc = theater.getCity();
        thr = theater.getState();
        thz = theater.getZip();
        tha = theater.getAddress();
        String lts = currentTimeStamp();

        if(engagement.equalsIgnoreCase("ticket_purchase_attempt")){
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Date date = new Date();
            String dateString = dateFormat.format(date);
            //Get current time
            SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
            String timeString = sdf.format(new Date());
            UserPreferences.setLastCheckInAttempt(dateString,timeString);
        }

        String result = "";
        thd = "";
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
        try {
            Date date = format1.parse(screening.getDate());
            SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            result = format2.format(date);
            thd = result;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        RestClient.getAuthenticatedAPIGoWatchIt().ticketPurchase(engagement, tht, thd, tn, thc, thr, thz, tha, "Movie",
                movieId, campaign, "app", "android", url, "organic",
                l, ln, userId, IDFA, versionCode, versionName, lts).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
                if (response != null && response.isSuccessful()) {
                    GoWatchItResponse responseBody = response.body();
                    Log.d("GO WATCH IT CHECK IN", "onResponse: " + responseBody.getMessage());
                }


            }

            @Override
            public void failure(RestError restError) {
//                progress.setVisibility(View.GONE);
                //Toast.makeText(MovieActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void searchEvent(String search, String engagement, String url) {

        String userId = String.valueOf(UserPreferences.getUserId());
        IDFA = UserPreferences.getAAID();

        String versionName = BuildConfig.VERSION_NAME;
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        String campaign = GoWatchItSingleton.getInstance().getCampaign();
        String lts = currentTimeStamp();


        RestClient.getAuthenticatedAPIGoWatchIt().searchTheatersMovies(engagement,
                "Movie", "-1", search, campaign, "app", "android", url, "organic",
                l, ln, userId, IDFA, versionCode, versionName, lts).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
                if (response != null && response.isSuccessful()) {
                    GoWatchItResponse responseBody = response.body();
                    Log.d("GO WATCH IT SEARCH", "onResponse: " + responseBody.getMessage());
                }
            }

            @Override
            public void failure(RestError restError) {
//                progress.setVisibility(View.GONE);
                // Toast.makeText(MovieActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void userOpenedTheater(Theater theaterObject, String url) {

        String userId = String.valueOf(UserPreferences.getUserId());
        IDFA = UserPreferences.getAAID();

        String versionName = BuildConfig.VERSION_NAME;
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);

        String campaign = GoWatchItSingleton.getInstance().getCampaign();
        String lts = currentTimeStamp();


        RestClient.getAuthenticatedAPIGoWatchIt().openTheaterEvent("impression", theaterObject.getName(),
                theaterObject.getCity(), theaterObject.getState(), theaterObject.getZip(), theaterObject.getAddress(), "Theater", "-1", campaign, "app", "android", url, "organic",
                l, ln, userId, IDFA, versionCode, versionName, lts).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
                if (response != null && response.isSuccessful()) {
                    GoWatchItResponse responseBody = response.body();
                    Log.d("GO WATCH IT THEATER", "onResponse: " + responseBody.getMessage());
                }


            }

            @Override
            public void failure(RestError restError) {
//                progress.setVisibility(View.GONE);
                // Toast.makeText(MovieActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void userOpenedTheaterTab(String url, String et) {

        String userId = String.valueOf(UserPreferences.getUserId());
        IDFA = UserPreferences.getAAID();

        String versionName = BuildConfig.VERSION_NAME;
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        String campaign = GoWatchItSingleton.getInstance().getCampaign();
        String lts = currentTimeStamp();


        RestClient.getAuthenticatedAPIGoWatchIt().openMapEvent("engagement", "Unset", "-1", et, campaign, "app", "android", url, "organic",
                l, ln, userId, IDFA, versionCode, versionName, lts).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
                if (response != null && response.isSuccessful()) {
                    GoWatchItResponse responseBody = response.body();
                    Log.d("GO WATCH IT THEATER MAP", "onResponse: " + responseBody.getMessage());
                }


            }

            @Override
            public void failure(RestError restError) {
//                progress.setVisibility(View.GONE);
                // Toast.makeText(MovieActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }


}
