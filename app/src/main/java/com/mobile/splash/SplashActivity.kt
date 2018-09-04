package com.mobile.splash

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.mobile.UserPreferences
import com.mobile.activities.LogInActivity
import com.mobile.activities.OnboardingActivity
import com.mobile.home.HomeActivity
import com.mobile.responses.SubscriptionStatus.*
import com.moviepass.R
import dagger.android.AndroidInjection
import javax.inject.Inject

class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var presenter: SplashActivityPresenter

    var ID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val intent = intent
        getAAID().execute()

        if (intent?.extras?.get("uri") != null) {
            val url = intent.extras?.get("uri").toString().trim()
            val movieOrTheater = url.split("/".toRegex())

            if (movieOrTheater.get(4) == "movies") {
                val movieID = movieOrTheater.get(5)
                launchActivity(2, Integer.valueOf(movieID))
            }
        } else {
            launchActivity(2, -1)
        }


//        if (data != null && data.path.length >= 2) run {
//            var movieIdEncripted: String
//            var movieOrTheater: String
//            var url = data.path
//
//            var urlPath = data.pathSegments
//            var idLength: Int
//            if (urlPath.size >= 2) {
//                movieOrTheater = urlPath.get(1)
//                if (movieOrTheater.equals("movies")) {
//                    if (urlPath.size >= 3) {
//                        movieIdEncripted = urlPath.get(2)
//                        idLength = movieIdEncripted.length
//                        idLength = idLength - 5
//                        movieIdEncripted = movieIdEncripted.substring(2, idLength)
//                        val movieId = Integer.valueOf(movieIdEncripted)!!
//                        launchActivity(0, movieId)
//                    }
//                    if (urlPath.size >= 4) {
//                        val campaign = urlPath.get(3)
//                        //GoWatchItSingleton.getInstance().setCampaign(campaign)
//                    }
//                    launchActivity(0, -1)
//                } else if (movieOrTheater.equals("theaters")) {
//                    launchActivity(1, -1)
//                    if (urlPath.size >= 3) {
//                        val campaign = urlPath.get(2)
//                        //GoWatchItSingleton.getInstance().setCampaign(campaign)
//                    }
//                } else {
//                    val campaign = urlPath.get(1)
//                    //GoWatchItSingleton.getInstance().setCampaign(campaign)
//                    launchActivity(2, -1)
//                }
//            } else {
//                launchActivity(2, -1)
//            }
//
////            loadMovies()
//
//        }
//        else {
//            Log.d(">>>>>>>>>>>>>>>>>>>", "MADE IT")
////            launchActivity(2, -1)
//
//        }
    }

    fun launchActivity(typeMovie: Int, movieID: Int) {
        Handler().postDelayed({
            if (UserPreferences.userId == 0) {
                val i = Intent(this@SplashActivity, OnboardingActivity::class.java)
                startActivity(i)
                finish()
            } else {
                val restrictions = UserPreferences.restrictions
                when (restrictions.subscriptionStatus) {
                    ACTIVE, ACTIVE_FREE_TRIAL, PENDING_ACTIVATION, PENDING_FREE_TRIAL -> {
                        Crashlytics.setUserIdentifier(UserPreferences.userId.toString())
                        val i = HomeActivity.newIntent(this@SplashActivity, typeMovie)
                        i.putExtra("movieID", movieID)
                        startActivity(i);
                        finish()
                    }
                    else -> {
                        val i = Intent(this@SplashActivity, LogInActivity::class.java)
                        startActivity(i)
                        finish()
                    }
                }
            }

        }, SPLASH_TIME_OUT.toLong())
    }

    companion object {
        // Splash screen timer
        private val SPLASH_TIME_OUT = 1000
    }

    private inner class getAAID : AsyncTask<Void, String?, String?>() {
        override fun onPostExecute(result: String?) {
            result?.let {
                UserPreferences.saveAAID(it)
            }

        }

        override fun doInBackground(vararg strings: Void): String? {
            var adInfo: AdvertisingIdClient.Info? = null

            try {
                adInfo = AdvertisingIdClient.getAdvertisingIdInfo(applicationContext)

            } catch (e: Exception) {
                // Unrecoverable error connecting to Google Play services (e.g.,
                // the old version of the service doesn't support getting AdvertisingId).
            }
            if (adInfo?.id != null) {
                ID = adInfo.id
            }

            return ID
        }
    }


}
