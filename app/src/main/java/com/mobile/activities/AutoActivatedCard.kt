package com.mobile.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.mobile.Constants
import com.mobile.UserPreferences
import com.mobile.fragments.MovieFragment
import com.mobile.model.Screening
import com.moviepass.R
import kotlinx.android.synthetic.main.fr_mpcard_autoactivated.*
import org.parceler.Parcels

class AutoActivatedCard : AppCompatActivity() {


    var screeningObject: Screening? = null
    var selectedShowTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fr_mpcard_autoactivated)

        val arguments = intent ?: return
        arguments.let {
            screeningObject = it.getParcelableExtra(Constants.SCREENING)
            selectedShowTime = it.getStringExtra(Constants.SHOWTIME)
        }



        closebutton.setOnClickListener {
            if (!UserPreferences.getHasUserSeenCardActivationScreen() && !UserPreferences.getRestrictionSubscriptionStatus().equals("ACTIVE")) {
                UserPreferences.setUserHasSeenCardActivationScreen(true)
                val activatedIntent = Intent(this, ActivatedCard_TutorialActivity::class.java)
                activatedIntent.putExtra(MovieFragment.SCREENING, Parcels.wrap<Screening>(screeningObject))
                activatedIntent.putExtra(Constants.SHOWTIME, selectedShowTime)
                startActivity(activatedIntent)
                finish()
            } else {
                UserPreferences.setUserHasSeenCardActivationScreen(true)
                finish()
            }

        }

    }
}
