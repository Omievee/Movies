package com.mobile.fragments

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.app.FragmentManager
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.mobile.UserPreferences
import com.mobile.profile.ProfilePresentation
import com.mobile.seats.MPBottomSheetFragment
import com.mobile.seats.SheetData
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_account_quota_container.view.*

open class PeakContainerView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {


    init {
        inflate(context, R.layout.layout_account_quota_container, this)
        layoutParams = MarginLayoutParams(MATCH_PARENT,WRAP_CONTENT)
    }

    open fun bind(pres:ProfilePresentation, childFragmentManager: FragmentManager) {
        val hasNewPeakPass = UserPreferences.hasNewPeakPass
        val pinfo = UserPreferences.restrictions.peakPassInfo

        amount.text = resources.getQuantityString(R.plurals.passes, pinfo.peakPasses.size, pinfo.peakPasses.size)
        if (TextUtils.isEmpty(pinfo.nextRefillDate)) {
            description.visibility = View.GONE
        } else {
            description.visibility = if (pinfo.peakPasses.isEmpty()) View.VISIBLE else View.GONE
            description.text = resources.getString(R.string.next_pass_applied, pinfo.nextRefillDate)
        }
        infoIcon.visibility = View.VISIBLE
        setOnClickListener { v ->
            val cpp = pinfo.currentPeakPass
            MPBottomSheetFragment.newInstance(
                    SheetData(
                            resources.getString(R.string.peak_pass),
                            resources.getString(R.string.peak_pass_apply_bottom), null,
                            if (cpp?.expires == null)
                                null
                            else
                                resources.getString(R.string.peak_pass_expires, cpp.expiresAsString()),
                            Gravity.CENTER
                    )
            ).show(childFragmentManager, "")
        }
        newKey.visibility = if (hasNewPeakPass) View.VISIBLE else View.GONE
    }
}