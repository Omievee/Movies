package com.mobile.activities

import android.content.Intent
import android.os.AsyncTask
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.helpshift.support.Log
import android.widget.Toast
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.mobile.Constants

import com.mobile.UserPreferences
import com.mobile.helpers.GoWatchItSingleton
import com.mobile.model.Eid
import com.mobile.network.Api
import com.mobile.requests.OpenAppEventRequest
import com.mobile.responses.GoWatchItResponse
import com.moviepass.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class SplashActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val intent = intent
        val data = intent.data

        getAAID().execute()
        if (data != null && data.path.length >= 2) run {
            var movieIdEncripted: String
            var movieOrTheater: String
            var url = data.path

            var urlPath = data.pathSegments
            var idLength: Int
            if(urlPath.size>=2) {
                movieOrTheater = urlPath.get(1)
                if(movieOrTheater.equals("movies")) {
                    if(urlPath.size>=3) {
                        movieIdEncripted = urlPath.get(2)
                        idLength = movieIdEncripted.length
                        idLength = idLength - 5
                        movieIdEncripted = movieIdEncripted.substring(2, idLength)
                        val movieId = Integer.valueOf(movieIdEncripted)!!
                        launchActivity(0,movieId)
                    }
                    if(urlPath.size>=4){
                        val campaign = urlPath.get(3)
                        GoWatchItSingleton.getInstance().campaign = campaign
                    }
                    launchActivity(0,-1)
                } else if(movieOrTheater.equals("theaters")){
                    launchActivity(1,-1)
                    if(urlPath.size>=3){
                        val campaign = urlPath.get(2)
                        GoWatchItSingleton.getInstance().campaign = campaign
                    }
                }
                else {
                    val campaign = urlPath.get(1)
                    GoWatchItSingleton.getInstance().campaign = campaign
                    launchActivity(2,-1)
                }
            }
            else {
                launchActivity(2, -1)
            }

            GoWatchItSingleton.getInstance().userOpenedApp(this, url)
//            loadMovies()

        }
        else {
            launchActivity(2, -1)
            var url = "https://www.moviepass.com/go"
            GoWatchItSingleton.getInstance().userOpenedApp(this, url)
        }



    }

    fun launchActivity(typeMovie:Int, id:Int){
        Handler().postDelayed({
            if (UserPreferences.getUserId() == 0 || UserPreferences.getUserId().equals("")) {
                val i = Intent(this@SplashActivity, OnboardingActivity::class.java)
                startActivity(i)
                finish()
            } else {
                if(UserPreferences.getRestrictionSubscriptionStatus().equals(Constants.ACTIVE) || UserPreferences.getRestrictionSubscriptionStatus().equals(Constants.ACTIVE_FREE_TRIAL) || UserPreferences.getRestrictionSubscriptionStatus().equals(Constants.PENDING_ACTIVATION)) {
                    if (typeMovie == 0) {
                        val i = Intent(this@SplashActivity, MoviesActivity::class.java)
                        i.putExtra(MoviesActivity.MOVIES, id)
                        startActivity(i)
                        finish()
                    }
                    if (typeMovie == 1) {
                        val i = Intent(this@SplashActivity, TheatersActivity::class.java)
                        i.putExtra(TheatersActivity.THEATER, id)
                        startActivity(i)
                        finish()
                    }
                    if (typeMovie == 2) {
                        val i = Intent(this@SplashActivity, MoviesActivity::class.java)
                        startActivity(i)
                        finish()
                    }
                }else{
                    val i = Intent(this@SplashActivity, LogInActivity::class.java)
                    startActivity(i)
                    finish()
                }

            }
        }, SPLASH_TIME_OUT.toLong())
    }

    companion object {
        // Splash screen timer
        private val SPLASH_TIME_OUT = 1000
    }

    private  inner class getAAID : AsyncTask<String, String, String>() {
        override fun onPostExecute(result: String) {
            UserPreferences.saveAAID(result)
        }

        override fun doInBackground(vararg strings: String): String {
            var adInfo: AdvertisingIdClient.Info? = null
            try {
                adInfo = AdvertisingIdClient.getAdvertisingIdInfo(applicationContext)

            } catch (e: IOException) {
                // Unrecoverable error connecting to Google Play services (e.g.,
                // the old version of the service doesn't support getting AdvertisingId).
            } catch (e: GooglePlayServicesNotAvailableException) {
                // Google Play services is not available entirely.
            } catch (e: GooglePlayServicesRepairableException) {
                e.printStackTrace()
            }

            val id = adInfo!!.id
            val isLAT = adInfo.isLimitAdTrackingEnabled

            return id
        }
    }


}
