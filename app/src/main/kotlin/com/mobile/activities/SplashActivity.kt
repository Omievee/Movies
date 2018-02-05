package com.mobile.activities

import android.content.Intent
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.mobile.UserPreferences
import com.moviepass.R

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
