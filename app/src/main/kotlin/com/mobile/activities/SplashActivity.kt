package com.mobile.activities

import android.content.Intent
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.mobile.UserPreferences
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

        Handler().postDelayed({
            if (UserPreferences.getUserId() == 0 || UserPreferences.getUserId().equals("")) {

                val i = Intent(this@SplashActivity, OnboardingActivity::class.java)
                startActivity(i)
                finish()
            } else {
                val i = Intent(this@SplashActivity, MoviesActivity::class.java)
                startActivity(i)
                finish()
            }
        }, SPLASH_TIME_OUT.toLong())
    }

    companion object {
        // Splash screen timer
        private val SPLASH_TIME_OUT = 1000
    }


}
