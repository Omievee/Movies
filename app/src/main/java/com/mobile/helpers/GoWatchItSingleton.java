package com.mobile.helpers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.mobile.UserPreferences;
import com.mobile.activities.MoviesActivity;
import com.mobile.model.Screening;
import com.mobile.model.Theater;
import com.mobile.network.RestCallback;
import com.mobile.network.RestClient;
import com.mobile.network.RestError;
import com.mobile.responses.GoWatchItResponse;
import com.moviepass.BuildConfig;

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

    public void userOpenedApp(Context context, String deepLink){

        String l = String.valueOf(UserPreferences.getLatitude());
        String ln = String.valueOf(UserPreferences.getLongitude());
        String userId = String.valueOf(UserPreferences.getUserId());
        if(deepLink==null)
            deepLink = "https://www.moviepass.com/go";
        String thisCampaign = GoWatchItSingleton.getInstance().getCampaign();

        String versionName = BuildConfig.VERSION_NAME;
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);


        RestClient.getAuthenticatedAPIGoWatchIt().openAppEvent("true","Unset",
                "-1","app_open",thisCampaign,"app","android",deepLink,"organic",
                l,ln,userId,"IDFA", versionCode, versionName).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
                GoWatchItResponse responseBody = response.body();

                Log.d("HEADER OPENED", "onResponse: "+responseBody.getFollowUrl());
            }

            @Override
            public void failure(RestError restError) {
                Toast.makeText(context, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void userOpenedMovie(String movieId, String url){

        String l = String.valueOf(UserPreferences.getLatitude());
        String ln = String.valueOf(UserPreferences.getLongitude());
        String userId = String.valueOf(UserPreferences.getUserId());

        String versionName = BuildConfig.VERSION_NAME;
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        String campaign = GoWatchItSingleton.getInstance().getCampaign();


        RestClient.getAuthenticatedAPIGoWatchIt().openAppEvent("true","Movie",
                String.valueOf(movieId),"impression",campaign,"app","android",url,"organic",
                l,ln,userId,"IDFA", versionCode, versionName).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
                GoWatchItResponse responseBody = response.body();
//                progress.setVisibility(View.GONE);

                Log.d("HEADER MOVIE -- >", "onResponse: "+responseBody.getFollowUrl());
            }

            @Override
            public void failure(RestError restError) {
//                progress.setVisibility(View.GONE);
                // Toast.makeText(MovieActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void userClickedOnShowtime(Theater theater, Screening screening, String showtime, String movieId, String url) {

        String l = String.valueOf(UserPreferences.getLatitude());
        String ln = String.valueOf(UserPreferences.getLongitude());
        String userId = String.valueOf(UserPreferences.getUserId());

        String versionName = BuildConfig.VERSION_NAME;
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        String tht,thd,tn,thc,thr,thz,tha;
        tht = showtime.trim();
        tn = screening.getTheaterName();
        thc = theater.getCity();
        thr = theater.getState();
        thz = theater.getZip();
        tha = theater.getAddress();

        String result="";
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

        RestClient.getAuthenticatedAPIGoWatchIt().clickOnShowtime("engagement","theater_click",tht,thd,tn,thc,thr,thz,tha,"true","Movie",
                movieId,campaign,"app","android",url,"organic",
                l,ln,userId,"IDFA", versionCode, versionName).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
                GoWatchItResponse responseBody = response.body();
//                progress.setVisibility(View.GONE);

                Log.d("HEADER MOVIE CLICK -- >", "onResponse: "+responseBody.getFollowUrl());
            }

            @Override
            public void failure(RestError restError) {
//                progress.setVisibility(View.GONE);
                // Toast.makeText(MovieActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void checkInEvent(Theater theater, Screening screening, String showtime, String engagement, String movieId, String url) {

        String l = String.valueOf(UserPreferences.getLatitude());
        String ln = String.valueOf(UserPreferences.getLongitude());
        String userId = String.valueOf(UserPreferences.getUserId());

        String versionName = BuildConfig.VERSION_NAME;
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        String tht,thd,tn,thc,thr,thz,tha;
        tht = showtime.trim();
        tn = screening.getTheaterName();
        thc = theater.getCity();
        thr = theater.getState();
        thz = theater.getZip();
        tha = theater.getAddress();

        String result="";
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

        RestClient.getAuthenticatedAPIGoWatchIt().ticketPurchase(engagement,tht,thd,tn,thc,thr,thz,tha,"true","Movie",
                movieId,campaign,"app","android",url,"organic",
                l,ln,userId,"IDFA", versionCode, versionName).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
                GoWatchItResponse responseBody = response.body();
//                progress.setVisibility(View.GONE);

                Log.d("HEADER MOVIE BUY -- >", "onResponse: "+responseBody.getFollowUrl());
            }

            @Override
            public void failure(RestError restError) {
//                progress.setVisibility(View.GONE);
                //Toast.makeText(MovieActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void searchEvent(String search, String engagement, String url){

        String l = String.valueOf(UserPreferences.getLatitude());
        String ln = String.valueOf(UserPreferences.getLongitude());
        String userId = String.valueOf(UserPreferences.getUserId());

        String versionName = BuildConfig.VERSION_NAME;
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        String campaign = GoWatchItSingleton.getInstance().getCampaign();


        RestClient.getAuthenticatedAPIGoWatchIt().searchTheatersMovies(engagement,"true",
                "Movie","-1",search,campaign,"app","android",url,"organic",
                l,ln,userId,"IDFA", versionCode, versionName).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
                GoWatchItResponse responseBody = response.body();
//                progress.setVisibility(View.GONE);

                Log.d("HEADER SEARCH -- >", "onResponse: "+responseBody.getFollowUrl());
            }

            @Override
            public void failure(RestError restError) {
//                progress.setVisibility(View.GONE);
                // Toast.makeText(MovieActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void userOpenedTheater(Theater theaterObject, String url){

        String l = String.valueOf(UserPreferences.getLatitude());
        String ln = String.valueOf(UserPreferences.getLongitude());
        String userId = String.valueOf(UserPreferences.getUserId());

        String versionName = BuildConfig.VERSION_NAME;
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);

        String campaign = GoWatchItSingleton.getInstance().getCampaign();


        RestClient.getAuthenticatedAPIGoWatchIt().openTheaterEvent("impression",theaterObject.getName(),
                theaterObject.getCity(),theaterObject.getState(),theaterObject.getZip(),theaterObject.getAddress(),"true","Theater","-1",campaign,"app","android",url,"organic",
                l,ln,userId,"IDFA", versionCode, versionName).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
                GoWatchItResponse responseBody = response.body();
//                progress.setVisibility(View.GONE);

                Log.d("HEADER THEATER -- >", "onResponse: "+responseBody.getFollowUrl());
            }

            @Override
            public void failure(RestError restError) {
//                progress.setVisibility(View.GONE);
                // Toast.makeText(MovieActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void userOpenedTheaterTab(String url){

        String l = String.valueOf(UserPreferences.getLatitude());
        String ln = String.valueOf(UserPreferences.getLongitude());
        String userId = String.valueOf(UserPreferences.getUserId());

        String versionName = BuildConfig.VERSION_NAME;
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        String campaign = GoWatchItSingleton.getInstance().getCampaign();


        RestClient.getAuthenticatedAPIGoWatchIt().openMapEvent("engagement","true","Unset","-1","map_view_click",campaign,"app","android",url,"organic",
                l,ln,userId,"IDFA", versionCode, versionName).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
                GoWatchItResponse responseBody = response.body();
//                progress.setVisibility(View.GONE);

                Log.d("HEADER THEATER MAP -- >", "onResponse: "+responseBody.getFollowUrl());
            }

            @Override
            public void failure(RestError restError) {
//                progress.setVisibility(View.GONE);
                // Toast.makeText(MovieActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}
