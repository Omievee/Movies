package com.mobile.onboard

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.fragments.MPFragment
import com.mobile.fragments.NearMe
import com.moviepass.R
import kotlinx.android.synthetic.main.fragment_onboarding.*

class OnboardingFragment : MPFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val section = arguments?.getParcelable<OnboardData>(ARG_SECTION_NUMBER) ?: return
        val context = context ?: return

        title.setText(section.header)

        titleContainer.visibility = when(title.length()==0) {
            true->View.GONE
            else->View.VISIBLE
        }
        body.setText(section.body)

        section_img.setImageResource(section.circleImage)

        logoContainer.visibility = when (section.showLogo) {
            true -> View.VISIBLE
            else -> View.GONE
        }

        findTheaters.setOnClickListener {
            NearMe().show(childFragmentManager,"")
        }

        findTheaters.visibility = when (section.findTheaters) {
            true -> View.VISIBLE
            else -> View.INVISIBLE
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding, container, false)
    }

    companion object {
        private val ARG_SECTION_NUMBER = "section_number"


        fun newInstance(sectionNumber: OnboardData): OnboardingFragment {
            return OnboardingFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}