package com.mobile.billing

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.fragments.MPFragment
import com.mobile.network.BillingApi
import com.mobile.profile.ProfileCancellationFragment
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_change_billing_and_plan_info.*
import java.text.SimpleDateFormat
import javax.inject.Inject

class ChangeBillingAndPlanInfoFragment : MPFragment() {

    var subscription: SubscriptionData? = null
    var subscriptionSub: Disposable? = null

    @Inject
    lateinit var billingApi: BillingApi

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_change_billing_and_plan_info, container, false)
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptionSub?.dispose()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancelPlan.setOnClickListener {
            showFragment(ProfileCancellationFragment())
        }
        billingChange.setOnClickListener {
            showFragment(MissingBillingFragment())
        }
    }

    override fun onResume() {
        super.onResume()
        if (subscription == null) {
            subscriptionSub?.dispose()
            subscriptionSub =
                    billingApi
                            .getSubscription()
                            .doAfterTerminate {
                                progress.visibility = View.GONE
                            }
                            .subscribe({
                                this.subscription = it.data
                                activity ?: return@subscribe
                                setData()
                            }, {
                                it.printStackTrace()
                            })
        } else {
            setData()

        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun setData() {
        val plan = subscription?.plan?.name
        userPlan.text = plan
        val cardNum = subscription?.billingInfo?.creditCardInfo?.cardNumber
        userBilling.text = cardNum
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy")

        val paidThroughDate = subscription?.paidThroughDate
        when (paidThroughDate) {
            null -> billingDate.text = "Unknown"
            else -> billingDate.text = dateFormat.format(paidThroughDate)
        }
    }

}