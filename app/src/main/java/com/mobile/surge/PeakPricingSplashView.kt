package com.mobile.surge

import android.app.Activity
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.View
import com.mobile.UserPreferences
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_peak_pricing_onboard.view.*

class PeakPricingSplashView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(ContextThemeWrapper(context, R.style.ReservationBackground), attrs,R.style.ReservationBackground) {

    init {
        View.inflate(context, R.layout.layout_peak_pricing_onboard, this)
        willSurgeIcon.isEnabled = false
        setOnClickListener {  }
        closeButton.setOnClickListener {
            (context as? Activity)?.let {
                it.onBackPressed()
            }
        }
        if(UserPreferences.shownPeakPricing) {
            visibility = View.GONE
        }
    }
}