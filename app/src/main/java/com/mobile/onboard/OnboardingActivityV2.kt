package com.mobile.onboard

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.DrawableRes

import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.WindowManager
import android.webkit.WebViewFragment
import com.facebook.login.Login

import com.mobile.MPActivty
import com.mobile.activities.LogInActivity
import com.mobile.utils.showFragment
import com.moviepass.R
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_onboarding.*

class OnboardingActivityV2 : MPActivty() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        window?.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        viewPager.adapter = OnboardingPagerAdapter(supportFragmentManager)

        logIn.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
        }
        signUp.setOnClickListener {
            showFragment(com.mobile.fragments.WebViewFragment())
        }
    }

}

@Parcelize
class OnboardData(
        @StringRes val header: Int,
        @StringRes val body: Int,
        @DrawableRes val circleImage: Int,
        val findTheaters: Boolean,
        val showLogo: Boolean = false
) : Parcelable

class OnboardingPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    val data: List<OnboardData> = listOf(
            OnboardData(
                    showLogo = true,
                    header = R.string.activity_onboarding_header_1,
                    body = R.string.activity_onboarding_body_1,
                    circleImage = R.drawable.image_onboarding_1,
                    findTheaters = true
            ),
            OnboardData(
                    header = R.string.activity_onboarding_header_2,
                    body = R.string.activity_onboarding_body_2,
                    circleImage = R.drawable.image_onboarding_2,
                    findTheaters = true
            ),
            OnboardData(
                    header = R.string.activity_onboarding_header_4,
                    body = R.string.activity_onboarding_body_4,
                    circleImage = R.drawable.image_onboarding_4,
                    findTheaters = false
            ),
            OnboardData(
                    header = R.string.activity_onboarding_header_5,
                    body = R.string.activity_onboarding_body_5,
                    circleImage = R.drawable.image_onboarding_5,
                    findTheaters = false
            )
    )

    override fun getItem(position: Int): Fragment {
        return OnboardingFragment.newInstance(data[position])
    }

    override fun getCount(): Int {
        return data.size
    }
}

fun updateIndicators(position: Int) {
//        for (i in indicators.indices) {
//            indicators[i].setBackgroundResource(
//                    if (i == position) R.drawable.indicator_selected_no_stroke else R.drawable.indicator_unselected_no_stroke
//            )
//        }
}


