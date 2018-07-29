package com.mobile.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.mobile.Constants
import com.mobile.UserPreferences
import com.mobile.model.Screening
import com.mobile.responses.SubscriptionStatus
import com.moviepass.R
import kotlinx.android.synthetic.main.fr_mpcard_autoactivated.*

class AutoActivatedCard : AppCompatActivity() {


    var screeningObject: Screening? = null
    var selectedShowTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fr_mpcard_autoactivated)
        val arguments = intent ?: return


        screeningObject = arguments.getParcelableExtra(Constants.SCREENING)
        selectedShowTime = arguments.getStringExtra(Constants.SHOWTIME)

        closebutton.setOnClickListener {
            checkPreference()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        checkPreference()
    }

    fun checkPreference() {
        if (!UserPreferences.hasUserSeenCardActivationScreen && !UserPreferences.restrictions.subscriptionStatus.equals(SubscriptionStatus.ACTIVE)) {
            UserPreferences.setUserHasSeenCardActivationScreen(true)
            val activatedIntent = Intent(this, ActivatedCardTutorialActivity::class.java)
            activatedIntent.putExtra(Constants.SCREENING, screeningObject)
            activatedIntent.putExtra(Constants.SHOWTIME, selectedShowTime)
            startActivity(activatedIntent)
            finish()
        } else {
            UserPreferences.setUserHasSeenCardActivationScreen(true)
            finish()
        }
    }
}
