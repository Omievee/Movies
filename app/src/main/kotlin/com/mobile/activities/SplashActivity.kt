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
                userOpenedApp()
                startActivity(i)
                finish()
            }
        }, SPLASH_TIME_OUT.toLong())
    }

    companion object {
        // Splash screen timer
        private val SPLASH_TIME_OUT = 1000
    }

    fun userOpenedApp(){
        val retrofit = Retrofit.Builder()
                .baseUrl("https://click.moviepass.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()


        val service = retrofit.create(Api::class.java!!)

        val eid = Eid("11111","IDFA")

        val openAppEventR = OpenAppEventRequest("Unset","-1","app_open","no_campaign","app","android",
                "url","organic","API-KEY","40.01","-74.01", eid)


        val userCall = service.openAppEvent(openAppEventR);

        Log.d("sdsd", "getWeatherInfo: " + userCall.request().url().toString())

        userCall.enqueue(object : Callback<GoWatchItResponse> {
            override fun onResponse(call: Call<GoWatchItResponse>, response: Response<GoWatchItResponse>) {
                if (response.body() == null && !response.isSuccessful()) {
                    Toast.makeText(this@SplashActivity, "Please try another city", Toast.LENGTH_SHORT).show()
                } else {

                }
            }

            override fun onFailure(call: Call<GoWatchItResponse>, t: Throwable) {
                Toast.makeText(this@SplashActivity, "Unable", Toast.LENGTH_SHORT).show()
                Log.d("sdsdf", "onFailure: ")
                t.printStackTrace()
            }
        })
    }

}
