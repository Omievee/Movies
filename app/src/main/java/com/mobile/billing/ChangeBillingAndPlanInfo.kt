package com.mobile.billing

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.UserPreferences
import com.mobile.fragments.MPFragment
import com.mobile.profile.ProfileCancellationFragment
import com.moviepass.R
import kotlinx.android.synthetic.main.fragment_change_billing_and_plan_info.*
import java.text.SimpleDateFormat

class ChangeBillingAndPlanInfo : MPFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_change_billing_and_plan_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancelPlan.setOnClickListener { showFragment(ProfileCancellationFragment()) }
        billingChange.setOnClickListener { showFragment(MissingBillingFragment()) }
        setData()
    }

    @SuppressLint("SimpleDateFormat")
    private fun setData() {
        val plan = UserPreferences.userInfo.plan
        userPlan.text = plan
        val cardNum = UserPreferences.userInfo.billingCard
        userBilling.text = cardNum
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy")

        when (UserPreferences.userInfo.nextBillingDate) {
            null -> billingDate.text = "Unknown"
            else -> billingDate.text = dateFormat.format(UserPreferences.userInfo.nextBillingDate)
        }
    }

}
