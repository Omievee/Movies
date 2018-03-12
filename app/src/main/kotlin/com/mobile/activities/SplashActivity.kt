package com.mobile.activities

import android.content.Intent
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

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

class SplashActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val intent = intent
        val data = intent.data
        if (data != null && data.path.length >= 2) run {
            var movieIdEncripted: String
            var movieOrTheater: String
            var url = data.path

            var urlPath = data.pathSegments
            var idLength: Int
            if(urlPath.size>=2) {
                movieOrTheater = urlPath.get(1)
                if(movieOrTheater.equals("movies")) {
                    movieIdEncripted = urlPath.get(2)
                    idLength = movieIdEncripted.length
                    idLength = idLength - 5
                    movieIdEncripted = movieIdEncripted.substring(2, idLength)
                    val movieId = Integer.valueOf(movieIdEncripted)!!
                    launchActivity(0,movieId)
                    if(urlPath.size>=4){
                        val campaign = urlPath.get(3)
                        GoWatchItSingleton.getInstance().campaign = campaign
                    }
                } else if(movieOrTheater.equals("theaters")){
                    launchActivity(1,-1)
                    if(urlPath.size>=3){
                        val campaign = urlPath.get(2)
                        GoWatchItSingleton.getInstance().campaign = campaign
                    }
                }
            }
            else {

            }

            GoWatchItSingleton.getInstance().userOpenedApp(this, url)
//            loadMovies()

        }
        else {
            launchActivity(2, -1)
        }


    }

    fun launchActivity(typeMovie:Int, id:Int){
        Handler().postDelayed({
            if (UserPreferences.getUserId() == 0 || UserPreferences.getUserId().equals("")) {
                val i = Intent(this@SplashActivity, OnboardingActivity::class.java)
                startActivity(i)
                finish()
            } else {
                if(typeMovie==0){
                    val i = Intent(this@SplashActivity, MoviesActivity::class.java)
                    i.putExtra(MoviesActivity.MOVIES,id)
                    startActivity(i)
                    finish()
                }
                if(typeMovie==1){
                    val i = Intent(this@SplashActivity, TheatersActivity::class.java)
                    i.putExtra(TheatersActivity.THEATER,id)
                    startActivity(i)
                    finish()
                }
                if(typeMovie==2){
                    val i = Intent(this@SplashActivity, MoviesActivity::class.java)
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


}
