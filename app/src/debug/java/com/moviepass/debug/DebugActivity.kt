package com.moviepass.debug

import android.os.Bundle
import com.mobile.MPActivty
import com.mobile.seats.SeatPreviewListener
import com.mobile.surge.PeakPricingActivity
import com.moviepass.R
import dagger.android.AndroidInjection

class DebugActivity : MPActivty(), SeatPreviewListener {

    override fun onClose() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)
        startActivity(PeakPricingActivity.newInstance(this))
    }
}
