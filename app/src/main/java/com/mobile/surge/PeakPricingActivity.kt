package com.mobile.surge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle

import com.mobile.MPActivty
import com.mobile.UserPreferences
import com.moviepass.R

class PeakPricingActivity : MPActivty() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_peak_pricing)
    }

    override fun onDestroy() {
        super.onDestroy()
        UserPreferences.setShownPeakPricing()
    }

    companion object {
        fun newInstance(context: Context) : Intent {
            return Intent(context, PeakPricingActivity::class.java)
        }

    }
}