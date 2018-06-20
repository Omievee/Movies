package com.mobile.fragments

import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.Constants
import com.mobile.UserPreferences
import com.mobile.activities.ActivatedCard_TutorialActivity
import com.mobile.helpers.LogUtils
import com.mobile.home.HomeActivity
import com.mobile.model.Screening
import com.moviepass.R
import kotlinx.android.synthetic.main.fr_mpcard_autoactivated.*
import org.parceler.Parcels

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AutoActivatedCardFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [AutoActivatedCardFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class AutoActivatedCardFragment : android.support.v4.app.Fragment() {


    var myContext: Context? = null
    var screeningObject: Screening? = null
    var selectedShowTime: String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_mpcard_autoactivated, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val i = arguments
        i.let {
            screeningObject = it?.getParcelable(Constants.SCREENING)
            selectedShowTime = it?.getString(Constants.SHOWTIME)
        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LogUtils.newLog("..>>>>>>>>>", "First boolean: " + UserPreferences.getHasUserSeenCardActivationScreen())
        closebutton.setOnClickListener {
            if (!UserPreferences.getHasUserSeenCardActivationScreen() && !UserPreferences.getRestrictionSubscriptionStatus().equals("ACTIVE")) {

                UserPreferences.setUserHasSeenCardActivationScreen(true)
                LogUtils.newLog("..>>>>>>>>>", "Second boolean: " + UserPreferences.getHasUserSeenCardActivationScreen())
                val activatedIntent = Intent(myContext, ActivatedCard_TutorialActivity::class.java)
                activatedIntent.putExtra(MovieFragment.SCREENING, Parcels.wrap<Screening>(screeningObject))
                activatedIntent.putExtra(Constants.SHOWTIME, selectedShowTime)
                startActivity(activatedIntent)
                LogUtils.newLog("..>>>>>>>>>", "second boolean: " + UserPreferences.getHasUserSeenCardActivationScreen())

            } else {
                UserPreferences.setUserHasSeenCardActivationScreen(true)
                val dismissScreen = Intent(myContext, HomeActivity::class.java)
                startActivity(dismissScreen)
            }

        }
    }


    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
                AutoActivatedCardFragment().apply {
                    arguments = Bundle().apply {
                    }
                }
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myContext = context
    }

}
