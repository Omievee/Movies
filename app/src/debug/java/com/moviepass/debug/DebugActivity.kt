package com.moviepass.debug

import android.os.Bundle
import com.mobile.MPActivty
import com.mobile.fragments.AlertScreenFragment
import com.mobile.model.*
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
        showFragment(AlertScreenFragment.newInstance(Alert(
                id = "foo",
                title = "A title that spans 1 if not 2 lines okie man i see",
                body = """As of August 15th, 2018 we are introducing a new pricing plan for all of our members that retains the features you love the most and removes the ones you don't. See up to three standard movies a month for $9.95 and get up to a $5.00 discount to any additional movie tickets purchased.

By clicking the 'I Accept' button below, you are choosing to remain a MoviePass subscriber under these new terms and will be charged $9.95 when your account renews on MM/DD/YY. If you do not wish to accept at this time, you may dismiss this message.""",
                urlTitle="Learn More",
                url = "https://moviepass.com/service_updates_20180727",
                dismissButton = true,
                dismissButtonText = "I Agree",
                dismissButtonWebhook = "http://google.com"
        )))
    }
}
