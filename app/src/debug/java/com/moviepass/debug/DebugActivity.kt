package com.moviepass.debug

import android.os.Bundle
import com.mobile.MPActivty
import com.mobile.model.Availability
import com.mobile.model.Screening
import com.mobile.model.Surge
import com.mobile.model.SurgeType
import com.mobile.screening.ScreeningPresentation
import com.mobile.seats.SeatPreviewListener
import com.moviepass.R
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.debug.activity_debug.*

class DebugActivity : MPActivty(), SeatPreviewListener {

    override fun onClose() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)
        val screening = Screening(approved = true)
        showtime.bind(
                Availability(startTime = "3:00 PM"),
                surge = Surge(level = SurgeType.SURGING),
                screening = ScreeningPresentation(
                        screening = screening,
                        selected = android.util.Pair(screening, "3:00 PM")
                )
        )
    }
}
