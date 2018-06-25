package com.mobile.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.Constants
import com.mobile.UserPreferences
import com.mobile.activities.ActivatedCard_TutorialActivity
import com.mobile.model.Screening
import com.moviepass.R
import kotlinx.android.synthetic.main.fr_mpcard_autoactivated.*
import org.parceler.Parcels


class AutoActivatedCardFragment : MPFragment() {


    var screeningObject: Screening? = null
    var selectedShowTime: String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_mpcard_autoactivated, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments = arguments ?: return
        arguments.let {
            screeningObject = it.getParcelable(Constants.SCREENING)
            selectedShowTime = it.getString(Constants.SHOWTIME)
        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        closebutton.setOnClickListener {
            if (!UserPreferences.getHasUserSeenCardActivationScreen() && !UserPreferences.getRestrictionSubscriptionStatus().equals("ACTIVE")) {

                UserPreferences.setUserHasSeenCardActivationScreen(true)
                val activatedIntent = Intent(context, ActivatedCard_TutorialActivity::class.java)
                activatedIntent.putExtra(MovieFragment.SCREENING, Parcels.wrap<Screening>(screeningObject))
                activatedIntent.putExtra(Constants.SHOWTIME, selectedShowTime)
                startActivity(activatedIntent)
            } else {
                UserPreferences.setUserHasSeenCardActivationScreen(true)
                activity?.onBackPressed()
            }

        }
    }


    companion object {
        @JvmStatic
        fun newInstance() =
                AutoActivatedCardFragment().apply {
                    arguments = Bundle().apply {
                    }
                }
    }


}
