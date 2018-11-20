package com.mobile.fragments

import android.content.Context
import android.support.v4.app.FragmentManager
import android.support.v4.widget.TextViewCompat
import android.util.AttributeSet
import android.view.View
import com.mobile.UserPreferences
import com.mobile.billing.Plan
import com.mobile.billing.Subscription
import com.mobile.profile.ProfilePresentation
import com.mobile.responses.UserInfoResponse
import com.mobile.seats.MPBottomSheetFragment
import com.mobile.seats.SheetData
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_account_quota_container.view.*
import java.text.SimpleDateFormat
import java.util.*

class CappedPlanView(context: Context?, attrs: AttributeSet? = null) : PeakContainerView(context, attrs) {

    override fun bind(pres:ProfilePresentation, childFragmentManager: FragmentManager) {
        val cap = UserPreferences.restrictions.cappedPlan ?: return
        header.text = resources.getString(R.string.movies_left_in_billing_cycle)
        amount.text = when (cap.isOverSoftCap) {
            true -> resources.getString(R.string.zero_movies)
            false -> resources.getQuantityString(R.plurals.movie_left, cap.remaining?:0, cap.remaining?:0)
        }
        description.text = when {
            cap.isOverHardCap -> resources.getString(R.string.over_hard_cap)
            cap.isOverSoftCap -> resources.getString(R.string.continue_seeing_movies)
            else -> null
        }
        description.visibility = View.VISIBLE
        infoIcon2.visibility = when {
            cap.isOverHardCap -> View.GONE
            else -> View.VISIBLE
        }
        description2.visibility = when((pres.data as? Subscription)?.data?.paidThroughDate!=null) {
            false->View.GONE
            true-> {
                description2.text = resources.getString(R.string.cycle_renews_on, SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format((pres.data as Subscription).data.paidThroughDate))
                View.VISIBLE}
        }
        TextViewCompat.setTextAppearance(description, R.style.ContinueSeeingMovies)
        descriptionContainer.visibility = when (cap.isOverSoftCap) {
            true -> View.VISIBLE
            else -> View.GONE
        }

        descriptionContainer.setOnClickListener {
            MPBottomSheetFragment.newInstance(SheetData(
                    title = resources.getString(R.string.continue_seeing_movies_title),
                    description = resources.getString(R.string.continue_seeing_movies_description, cap.used?:3)
            )).show(childFragmentManager, "")
        }
    }

}