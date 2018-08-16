package com.mobile.fragments

import android.content.Context
import android.os.Bundle
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.UserPreferences
import com.mobile.adapters.BasicDiffCallback
import com.mobile.model.UserInfo
import com.mobile.network.Api
import com.mobile.profile.*
import com.mobile.responses.UserInfoResponse
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_account_details.*
import javax.inject.Inject

/*
 * Created by anubis on 9/2/17.
 */

class AccountDetailsFragment : MPFragment() {

    var planResponse: UserInfoResponse? = null

    var planSub: Disposable? = null

    @Inject
    lateinit var api: Api

    val clickListener = object : ProfileClickListener {
        override fun onClick(pres: ProfilePresentation) {
            when (pres.type) {
                Profile.ACCOUNT_INFORMATION -> showFragment(ProfileAccountInformation())
                Profile.SHIPPING_ADDRESS -> showFragment(ProfileAccountShippingInformation())
                Profile.CHANGE_PASSWORD -> showFragment(ProfileAccountChangePassword())
                Profile.PLAN_AND_BILLING -> showFragment(ProfileAccountPlanAndBilling())
                else -> {
                }
            }
        }

    }

    val adapter: ProfileAdapter = ProfileAdapter(this, clickListener = clickListener)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_details, container, false)
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()
        adapter.data = data
        fetchUserInfo()
    }

    private fun fetchUserInfo() {
        if (planResponse != null) {
            return
        }
        planSub?.dispose()
        planSub = api.getUserDataRx(UserPreferences.userId).subscribe { t1, t2 ->
            planResponse = t1
            adapter.data = data
        }
    }

    val data: ProfileData
        get() {
            val old = adapter.data?.data ?: emptyList()
            val newData = mutableListOf(
                    ProfilePresentation(
                            type = Profile.ACCOUNT_INFORMATION,
                            title = getString(R.string.account_information)
                    ),
                    ProfilePresentation(
                            type = Profile.CHANGE_PASSWORD,
                            title = getString(R.string.change_password)
                    ),
                    ProfilePresentation(
                            type = Profile.SHIPPING_ADDRESS,
                            title = getString(R.string.shipping_address)
                    ),
                    ProfilePresentation(
                            type = Profile.PLAN_AND_BILLING,
                            title = getString(R.string.plan_and_billing_info)
                    )
            )
            when (UserPreferences.restrictions.cappedPlan) {
                null -> {
                }
                else -> newData.add(ProfilePresentation(type = Profile.CAPPED_PLAN, data = planResponse))
            }
            when (UserPreferences.restrictions.peakPassInfo.enabled) {
                true -> {
                    if(newData.any { it.type==Profile.CAPPED_PLAN }) {
                        newData.add(ProfilePresentation(type=Profile.DIVIDER))
                    }
                    newData.add(ProfilePresentation(
                            type = Profile.PEAK_PASS
                    )
                    )
                }
            }
            return ProfileData(newData, DiffUtil.calculateDiff(BasicDiffCallback(old, newData)))
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backButton.setOnClickListener { activity?.onBackPressed() }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = null
    }

}



