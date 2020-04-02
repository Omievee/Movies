package com.moviepass.debug

import android.os.Bundle
import com.mobile.MPActivty
import com.mobile.alertscreen.AlertScreenFragment
import com.mobile.model.Alert
import com.mobile.plans.ChangePlansFragment
import com.mobile.seats.SeatPreviewListener
import com.mobile.utils.showFragment
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

        showFragment(ChangePlansFragment())
    }
}
