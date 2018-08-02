package com.mobile.peakpass

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import com.mobile.MPActivty
import com.mobile.UserPreferences
import com.moviepass.R
import kotlinx.android.synthetic.main.fragment_add_guests.*

class PeakPassActivity : MPActivty() {

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_peak_pass_onboard)
        closeButton.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        UserPreferences.showPeakPassOnboard = false
    }

    companion object {

        fun newInstance(context: Context):Intent {
            return Intent(context, PeakPassActivity::class.java)
        }
    }

}
