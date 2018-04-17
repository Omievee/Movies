package com.mobile.helpers;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;
import com.mobile.Constants;
import com.mobile.model.Movie;
import com.mobile.model.Theater;
import com.mobile.network.RestClient;
import com.mobile.responses.AllMoviesResponse;
import com.mobile.responses.LocalStorageMovies;
import com.mobile.responses.LocalStorageTheaters;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by o_vicarra on 3/28/18.
 */

public class RealmTaskService extends GcmTaskService {


    Realm moviesRealm;
    Realm allMoviesRealm;
    RealmConfiguration config;
    RealmConfiguration allMoviesConfig;

    public static final String GCM_REPEAT_TAG = "repeat|[7200,1800]";
    private Realm tRealm;

    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        scheduleRepeatTask(this);
    }

    @Override
    public int onRunTask(TaskParams taskParams) {

        Bundle extras = taskParams.getExtras();
        Handler h = new Handler(getMainLooper());

        if (taskParams.getTag().equals(GCM_REPEAT_TAG)) {
            h.post(new Runnable() {
                @Override
                public void run() {
                    getTheatersBucket();
                    getMoviesBucket();
                    getAllMovies();
                }
            });
        }

        return GcmNetworkManager.RESULT_SUCCESS;
    }


    public static void scheduleRepeatTask(Context context) {
        try {
            PeriodicTask periodic = new PeriodicTask.Builder()
                    //specify target service - must extend GcmTaskService
                    .setService(RealmTaskService.class)
                    //repeat every 60 seconds
                    .setPeriod(7200)
                    //specify how much earlier the task can be executed (in seconds)
                    .setFlex(10000)
                    //tag that is unique to this task (can be used to cancel task)
                    .setTag(GCM_REPEAT_TAG)
                    //whether the task persists after device reboot
                    .setPersisted(true)
                    //set required network state, this line is optional
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .build();


            GcmNetworkManager.getInstance(context).schedule(periodic);
            Log.d(Constants.TAG, "repeating task scheduled");
        } catch (Exception e) {
            Log.e(Constants.TAG, "scheduling failed");
            e.printStackTrace();
        }
    }

    void getTheatersBucket() {

        tRealm = Realm.getDefaultInstance();

        RestClient.getLocalStorageAPI().getAllMoviePassTheaters().enqueue(new Callback<LocalStorageTheaters>() {
            @Override
            public void onResponse(Call<LocalStorageTheaters> call, Response<LocalStorageTheaters> response) {
                LocalStorageTheaters locallyStoredTheaters = response.body();
                if (locallyStoredTheaters != null && response.isSuccessful()) {

                  tRealm.executeTransactionAsync(R -> {

                        for (int j = 0; j < locallyStoredTheaters.getTheaters().size(); j++) {
                            Theater RLMTH = R.createObject(Theater.class, locallyStoredTheaters.getTheaters().get(j).getId());
                            RLMTH.setMoviepassId(locallyStoredTheaters.getTheaters().get(j).getMoviepassId());
                            RLMTH.setTribuneTheaterId(locallyStoredTheaters.getTheaters().get(j).getTribuneTheaterId());
                            RLMTH.setName(locallyStoredTheaters.getTheaters().get(j).getName());
                            RLMTH.setAddress(locallyStoredTheaters.getTheaters().get(j).getAddress());
                            RLMTH.setCity(locallyStoredTheaters.getTheaters().get(j).getCity());
                            RLMTH.setState(locallyStoredTheaters.getTheaters().get(j).getState());
                            RLMTH.setZip(locallyStoredTheaters.getTheaters().get(j).getZip());
                            RLMTH.setDistance(locallyStoredTheaters.getTheaters().get(j).getDistance());
                            RLMTH.setLat(locallyStoredTheaters.getTheaters().get(j).getLat());
                            RLMTH.setLon(locallyStoredTheaters.getTheaters().get(j).getLon());
                            RLMTH.setTicketType(locallyStoredTheaters.getTheaters().get(j).getTicketType());
                        }
                    }, () -> {
                        Log.d(Constants.TAG, "onSuccess: ");
                    }, error -> {
                        // Transaction failed and was automatically canceled.
                        Log.d(Constants.TAG, "Realm onError: " + error.getMessage());
                    });
                }
            }

            @Override
            public void onFailure(Call<LocalStorageTheaters> call, Throwable t) {
                Toast.makeText(RealmTaskService.this, "Error while downloading Theaters.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void getAllMovies(){
        Log.d(Constants.TAG, "getAllMovies: GETTING ALL MOVIES");
        allMoviesConfig = new RealmConfiguration.Builder()
                .name("AllMovies.Realm")
                .deleteRealmIfMigrationNeeded()
                .build();


        allMoviesRealm = Realm.getInstance(allMoviesConfig);
        RestClient.getLocalStorageAPI().getAllMovies().enqueue(new Callback<List<AllMoviesResponse>>() {
            @Override
            public void onResponse(Call<List<AllMoviesResponse>> call, Response<List<AllMoviesResponse>> response) {
                List<AllMoviesResponse> info = new ArrayList<>();
                info   = response.body();
                if (response.isSuccessful() && response != null) {
                    List<AllMoviesResponse> finalInfo = info;
                    allMoviesRealm.executeTransaction(realm -> {
                        for(AllMoviesResponse movie: finalInfo){
                            Movie newMovie = realm.createObject(Movie.class);
                            newMovie.setId(Integer.parseInt(movie.getId()));
                            newMovie.setTitle(movie.getTitle());
                            newMovie.setRunningTime(Integer.parseInt(movie.getRunningTime()));
                            newMovie.setRating(movie.getRating());
                            newMovie.setSynopsis(movie.getSynopsis());
                            newMovie.setImageUrl(movie.getImageUrl());
                            newMovie.setLandscapeImageUrl(movie.getLandscapeImageUrl());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<AllMoviesResponse>> call, Throwable t) {
            }
        });

    }

    void getMoviesBucket() {
        config = new RealmConfiguration.Builder()
                .name("Movies.Realm")
                .deleteRealmIfMigrationNeeded()
                .build();


        moviesRealm = Realm.getInstance(config);

        moviesRealm.executeTransactionAsync(realm -> realm.deleteAll());
        RestClient.getLocalStorageAPI().getAllCurrentMovies().enqueue(new Callback<LocalStorageMovies>() {
            @Override
            public void onResponse(Call<LocalStorageMovies> call, Response<LocalStorageMovies> response) {
                LocalStorageMovies localStorageMovies = response.body();
                if (localStorageMovies != null && response.isSuccessful()) {

                moviesRealm.executeTransactionAsync(realm -> {
                        for (int i = 0; i < localStorageMovies.getNewReleases().size(); i++) {
                            Movie newReleaseMovies = realm.createObject(Movie.class);
                            newReleaseMovies.setType("New Releases");
                            newReleaseMovies.setId(localStorageMovies.getNewReleases().get(i).getId());
                            newReleaseMovies.setRunningTime(localStorageMovies.getNewReleases().get(i).getRunningTime());
                            newReleaseMovies.setSynopsis(localStorageMovies.getNewReleases().get(i).getSynopsis());
                            newReleaseMovies.setImageUrl(localStorageMovies.getNewReleases().get(i).getImageUrl());
                            newReleaseMovies.setLandscapeImageUrl(localStorageMovies.getNewReleases().get(i).getLandscapeImageUrl());
                            newReleaseMovies.setTheaterName(localStorageMovies.getNewReleases().get(i).getTheaterName());
                            newReleaseMovies.setTitle(localStorageMovies.getNewReleases().get(i).getTitle());
                            newReleaseMovies.setTribuneId(localStorageMovies.getNewReleases().get(i).getTribuneId());
                            newReleaseMovies.setRating(localStorageMovies.getNewReleases().get(i).getRating());
                            newReleaseMovies.setTeaserVideoUrl(localStorageMovies.getNewReleases().get(i).getTeaserVideoUrl());


                        }
                        for (int i = 0; i < localStorageMovies.getNowPlaying().size(); i++) {
                            Movie nowPlayingMovies = realm.createObject(Movie.class);
                            nowPlayingMovies.setType("Now Playing");
                            nowPlayingMovies.setId(localStorageMovies.getNowPlaying().get(i).getId());
                            nowPlayingMovies.setRunningTime(localStorageMovies.getNowPlaying().get(i).getRunningTime());
                            nowPlayingMovies.setSynopsis(localStorageMovies.getNowPlaying().get(i).getSynopsis());
                            nowPlayingMovies.setImageUrl(localStorageMovies.getNowPlaying().get(i).getImageUrl());
                            nowPlayingMovies.setLandscapeImageUrl(localStorageMovies.getNowPlaying().get(i).getLandscapeImageUrl());
                            nowPlayingMovies.setTheaterName(localStorageMovies.getNowPlaying().get(i).getTheaterName());
                            nowPlayingMovies.setTitle(localStorageMovies.getNowPlaying().get(i).getTitle());
                            nowPlayingMovies.setTribuneId(localStorageMovies.getNowPlaying().get(i).getTribuneId());
                            nowPlayingMovies.setRating(localStorageMovies.getNowPlaying().get(i).getRating());
                            nowPlayingMovies.setTeaserVideoUrl(localStorageMovies.getNowPlaying().get(i).getTeaserVideoUrl());


                        }
                        for (int i = 0; i < localStorageMovies.getFeatured().size(); i++) {
                            Movie featuredMovie = realm.createObject(Movie.class);
                            featuredMovie.setType("Featured");
                            featuredMovie.setId(localStorageMovies.getFeatured().get(i).getId());
                            featuredMovie.setRunningTime(localStorageMovies.getFeatured().get(i).getRunningTime());
                            featuredMovie.setSynopsis(localStorageMovies.getFeatured().get(i).getSynopsis());
                            featuredMovie.setImageUrl(localStorageMovies.getFeatured().get(i).getImageUrl());
                            featuredMovie.setLandscapeImageUrl(localStorageMovies.getFeatured().get(i).getLandscapeImageUrl());
                            featuredMovie.setTheaterName(localStorageMovies.getFeatured().get(i).getTheaterName());
                            featuredMovie.setTitle(localStorageMovies.getFeatured().get(i).getTitle());
                            featuredMovie.setTribuneId(localStorageMovies.getFeatured().get(i).getTribuneId());
                            featuredMovie.setRating(localStorageMovies.getFeatured().get(i).getRating());
                            featuredMovie.setTeaserVideoUrl(localStorageMovies.getFeatured().get(i).getTeaserVideoUrl());


                        }


                        for (int i = 0; i < localStorageMovies.getComingSoon().size(); i++) {
                            Movie comingSoonMovies = realm.createObject(Movie.class);
                            comingSoonMovies.setType("Coming Soon");
                            comingSoonMovies.setId(localStorageMovies.getComingSoon().get(i).getId());
                            comingSoonMovies.setRunningTime(localStorageMovies.getComingSoon().get(i).getRunningTime());
                            comingSoonMovies.setSynopsis(localStorageMovies.getComingSoon().get(i).getSynopsis());
                            comingSoonMovies.setImageUrl(localStorageMovies.getComingSoon().get(i).getImageUrl());
                            comingSoonMovies.setLandscapeImageUrl(localStorageMovies.getComingSoon().get(i).getLandscapeImageUrl());
                            comingSoonMovies.setTheaterName(localStorageMovies.getComingSoon().get(i).getTheaterName());
                            comingSoonMovies.setTitle(localStorageMovies.getComingSoon().get(i).getTitle());
                            comingSoonMovies.setTribuneId(localStorageMovies.getComingSoon().get(i).getTribuneId());
                            comingSoonMovies.setCreatedAt(localStorageMovies.getComingSoon().get(i).getCreatedAt());
                            comingSoonMovies.setRating(localStorageMovies.getComingSoon().get(i).getRating());
                            comingSoonMovies.setReleaseDate(localStorageMovies.getComingSoon().get(i).getReleaseDate());
                            comingSoonMovies.setTeaserVideoUrl(localStorageMovies.getComingSoon().get(i).getTeaserVideoUrl());


                        }
                        for (int i = 0; i < localStorageMovies.getTopBoxOffice().size(); i++) {
                            Movie topBoxOfficeMovies = realm.createObject(Movie.class);
                            topBoxOfficeMovies.setType("Top Box Office");
                            topBoxOfficeMovies.setId(localStorageMovies.getTopBoxOffice().get(i).getId());
                            topBoxOfficeMovies.setRunningTime(localStorageMovies.getTopBoxOffice().get(i).getRunningTime());
                            topBoxOfficeMovies.setSynopsis(localStorageMovies.getTopBoxOffice().get(i).getSynopsis());
                            topBoxOfficeMovies.setImageUrl(localStorageMovies.getTopBoxOffice().get(i).getImageUrl());
                            topBoxOfficeMovies.setLandscapeImageUrl(localStorageMovies.getTopBoxOffice().get(i).getLandscapeImageUrl());
                            topBoxOfficeMovies.setTheaterName(localStorageMovies.getTopBoxOffice().get(i).getTheaterName());
                            topBoxOfficeMovies.setTitle(localStorageMovies.getTopBoxOffice().get(i).getTitle());
                            topBoxOfficeMovies.setTribuneId(localStorageMovies.getTopBoxOffice().get(i).getTribuneId());
                            topBoxOfficeMovies.setRating(localStorageMovies.getTopBoxOffice().get(i).getRating());
                            topBoxOfficeMovies.setTeaserVideoUrl(localStorageMovies.getTopBoxOffice().get(i).getTeaserVideoUrl());

                        }
                    }, () -> {

                        Log.d(Constants.TAG, "onSuccess: ");

                    }, error -> {
                        Log.d(Constants.TAG, "onResponse: " + error.getMessage());
                    });

                }
            }


            @Override
            public void onFailure(Call<LocalStorageMovies> call, Throwable t) {
                Toast.makeText(RealmTaskService.this, "Failure Updating Movies", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

