package com.moviepass.activities

import android.content.Intent
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.moviepass.R
import com.moviepass.UserPreferences

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            if (UserPreferences.getHasUserLoggedInBefore() || UserPreferences.getUserId() != 0) {
                val i = Intent(this@SplashActivity, BrowseActivity::class.java)
                startActivity(i)
                finish()
            } else {
                val i = Intent(this@SplashActivity, ProfileActivity::class.java)
                startActivity(i)
                finish()
            }
        }, SPLASH_TIME_OUT.toLong())
    }

    companion object {

        // Splash screen timer
        private val SPLASH_TIME_OUT = 2500
    }

}
