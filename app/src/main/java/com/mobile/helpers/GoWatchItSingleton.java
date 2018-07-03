package com.mobile.helpers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.gowatchit.GoWatchItApi;
import com.mobile.gowatchit.GoWatchItManager;
import com.mobile.model.Movie;
import com.mobile.model.Screening;
import com.mobile.model.Theater;
import com.mobile.network.RestCallback;
import com.mobile.network.RestClient;
import com.mobile.network.RestError;
import com.mobile.responses.AllMoviesResponse;
import com.mobile.responses.GoWatchItResponse;
import com.moviepass.BuildConfig;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Response;

import static com.mobile.UserPreferences.*;

/**
 * Created by ivonneortega on 3/11/18.
 */

public class GoWatchItSingleton implements GoWatchItManager {

    private static GoWatchItSingleton instance;
    private String campaign;
    private String debug = "false";
    private String l = "0.0";
    private String ln = "0.0";
    private String IDFA;
    private List<AllMoviesResponse> ALLMOVIES;
    private RealmResults<Movie> allMovies;
    private final GoWatchItApi api;
//    String l = String.valueOf(UserPreferences.getLatitude());
//    String ln = String.valueOf(UserPreferences.getLongitude());

    private GoWatchItSingleton() {
        getMovies();
        campaign = "no_campaign";
        api = RestClient.getAuthenticatedAPIGoWatchIt();
    }

    public GoWatchItSingleton(GoWatchItApi api) {
        this.api = api;
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(String campaign) {
        if (campaign != null) {
            this.campaign = campaign;
        }
    }

    public static GoWatchItSingleton getInstance() {

        synchronized (ContextSingleton.class) {
            if (instance == null) {
                instance = new GoWatchItSingleton();
            }
            return instance;
        }
    }

    public boolean isAllMoviesEmpty() {
        if (ALLMOVIES == null)
            return true;
        if (ALLMOVIES.size() == 0)
            return true;
        return false;
    }

    private String currentTimeStamp() {
        Long tsLong = System.currentTimeMillis() / 1000;
        String ts = tsLong.toString();
        return ts;
    }

    @Override
    public void userOpenedApp(Context context, String deepLink) {

        String userId = String.valueOf(INSTANCE.getUserId());
        if (deepLink == null)
            deepLink = "https://www.moviepass.com/go";
        String thisCampaign = GoWatchItSingleton.getInstance().getCampaign();

        String versionName = BuildConfig.VERSION_NAME;
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        String lts = currentTimeStamp();
        IDFA = INSTANCE.getAAID();

        api.openAppEvent("Unset",
                "-1", "-1", "app_open", thisCampaign, "app", "android", deepLink, "organic",
                l, ln, userId, IDFA, versionCode, versionName, lts).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
                if (response != null && response.isSuccessful()) {
                    GoWatchItResponse responseBody = response.body();
                    LogUtils.newLog("GO WATCH IT APP OPEN", "onResponse: " + responseBody.getMessage());
                }


            }

            @Override
            public void failure(RestError restError) {
                Toast.makeText(context, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void userOpenedMovie(String movieId, String url, String position) {

        Log.d(Constants.TAG, "userOpenedMovie: "+position
        );

        if(isAllMoviesEmpty())
            getMovies();
        String userId = String.valueOf(INSTANCE.getUserId());
        String versionName = BuildConfig.VERSION_NAME;
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        String campaign = GoWatchItSingleton.getInstance().getCampaign();
        String lts = currentTimeStamp();
        IDFA = INSTANCE.getAAID();
        LogUtils.newLog("WATCH", "userOpenedMovie: "+movieId);
        String movieTitle = getMovieTitle(movieId);

        RestClient.getAuthenticatedAPIGoWatchIt().openMovieEvent("Movie",
                String.valueOf(movieId),position,movieTitle, "impression", campaign, "app", "android", url, "organic",
                l, ln, userId, IDFA, versionCode, versionName, lts).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {

//                progress.setVisibility(View.GONE);

                if (response != null && response.isSuccessful()) {
                    GoWatchItResponse responseBody = response.body();
                    LogUtils.newLog("GO WATCH IT MOVIE", "onResponse: " + responseBody.getMessage());
                }


            }

            @Override
            public void failure(RestError restError) {
//                progress.setVisibility(View.GONE);
                // Toast.makeText(MovieActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void userClickedOnShowtime(Theater theater, Screening screening, String showtime, String movieId, String url) {

        if (isAllMoviesEmpty())
            getMovies();
        String userId = String.valueOf(INSTANCE.getUserId());
        IDFA = INSTANCE.getAAID();
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
        String movieTitle = getMovieTitle(movieId);

        String result = "";
        thd = "";
        try {
            SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            result = format2.format(screening.getDate());
            thd = result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        api.clickOnShowtime("engagement", "theater_click", tht, thd, tn, thc, thr, thz, tha, "Movie",
                movieId, movieTitle, campaign, "app", "android", url, "organic",
                l, ln, userId, IDFA, versionCode, versionName, lts).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
                if (response != null && response.isSuccessful()) {
                    GoWatchItResponse responseBody = response.body();
                    LogUtils.newLog("GO WATCH IT SHOWTIME", "onResponse: " + responseBody.getMessage());
                }


            }

            @Override
            public void failure(RestError restError) {
//                progress.setVisibility(View.GONE);
                // Toast.makeText(MovieFragment.this, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void checkInEvent(Theater theater, Screening screening, String showtime, String engagement, String movieId, String url) {

        if (isAllMoviesEmpty())
            getMovies();
        String userId = String.valueOf(INSTANCE.getUserId());
        IDFA = INSTANCE.getAAID();
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
        String movieTitle = getMovieTitle(movieId);
        INSTANCE.setLastCheckInAttemptDate();

        String result = "";
        thd = "";
        try {
            SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            result = format2.format(screening.getDate());
            thd = result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        api.ticketPurchase(engagement, tht, thd, tn, thc, thr, thz, tha, "Movie",
                movieId, movieTitle, campaign, "app", "android", url, "organic",
                l, ln, userId, IDFA, versionCode, versionName, lts).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
                if (response != null && response.isSuccessful()) {
                    GoWatchItResponse responseBody = response.body();
                    LogUtils.newLog("GO WATCH IT CHECK IN", "onResponse: " + responseBody.getMessage());
                }


            }

            @Override
            public void failure(RestError restError) {
//                progress.setVisibility(View.GONE);
                //Toast.makeText(MovieFragment.this, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void searchEvent(String search, String engagement, String url) {

        String userId = String.valueOf(INSTANCE.getUserId());
        IDFA = INSTANCE.getAAID();
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
                    LogUtils.newLog("GO WATCH IT SEARCH", "onResponse: " + responseBody.getMessage());
                }
            }

            @Override
            public void failure(RestError restError) {
//                progress.setVisibility(View.GONE);
                // Toast.makeText(MovieFragment.this, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void userOpenedTheater(Theater theaterObject, String url) {

        String userId = String.valueOf(INSTANCE.getUserId());
        IDFA = INSTANCE.getAAID();
        String versionName = BuildConfig.VERSION_NAME;
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        String campaign = GoWatchItSingleton.getInstance().getCampaign();
        String lts = currentTimeStamp();


        api.openTheaterEvent("impression", theaterObject.getName(),
                theaterObject.getCity(), theaterObject.getState(), theaterObject.getZip(), theaterObject.getAddress(), "Theater", "-1", campaign, "app", "android", url, "organic",
                l, ln, userId, IDFA, versionCode, versionName, lts).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
                if (response != null && response.isSuccessful()) {
                    GoWatchItResponse responseBody = response.body();
                    LogUtils.newLog("GO WATCH IT THEATER", "onResponse: " + responseBody.getMessage());
                }


            }

            @Override
            public void failure(RestError restError) {
//                progress.setVisibility(View.GONE);
                // Toast.makeText(MovieFragment.this, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void userOpenedTheaterTab(String url, String et) {

        String userId = String.valueOf(INSTANCE.getUserId());
        IDFA = INSTANCE.getAAID();

        String versionName = BuildConfig.VERSION_NAME;
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        String campaign = GoWatchItSingleton.getInstance().getCampaign();
        String lts = currentTimeStamp();


        api.openMapEvent("engagement", "Unset", "-1", et, campaign, "app", "android", url, "organic",
                l, ln, userId, IDFA, versionCode, versionName, lts).enqueue(new RestCallback<GoWatchItResponse>() {
            @Override
            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
                if (response != null && response.isSuccessful()) {
                    GoWatchItResponse responseBody = response.body();
                    LogUtils.newLog("GO WATCH IT THEATER MAP", "onResponse: " + responseBody.getMessage());
                }


            }

            @Override
            public void failure(RestError restError) {
//                progress.setVisibility(View.GONE);
                // Toast.makeText(MovieFragment.this, restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    String getMovieTitle(String id) {
        for (Movie movie : allMovies) {
            if (movie.getId() == Integer.parseInt(id)) {
                return movie.getTitle();
            }
        }
        return null;
    }

    @Override
    public void getMovies() {
        try {
            RealmConfiguration config = new RealmConfiguration.Builder()
                    .name("Movies.Realm")
                    .deleteRealmIfMigrationNeeded()
                    .build();
            Realm moviesRealm = Realm.getInstance(config);
            allMovies = moviesRealm.where(Movie.class)
                    .equalTo("type", "Top Box Office")
                    .or()
                    .equalTo("type", "New Releases")
                    .or()
                    .equalTo("type", "Coming Soon")
                    .or()
                    .equalTo("type", "Now Playing")
                    .or()
                    .equalTo("type", "Featured")
                    .findAll();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }


    }


}