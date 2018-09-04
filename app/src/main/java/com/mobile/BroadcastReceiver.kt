package com.mobile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mobile.activities.OnboardingActivity
import com.mobile.home.HomeActivity
import com.mobile.utils.startIntentIfResolves


class BroadcastReceiver : BroadcastReceiver() {

    private lateinit var URL: String

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return

        intent?.let {
            URL = it.getStringExtra(Constants.APPBOY_DEEP_LINK_KEY)
            when (UserPreferences.userId) {
                0 -> {
                    val receivedIntent = Intent(context, OnboardingActivity::class.java)
                    receivedIntent.putExtra(Constants.APPBOY_DEEP_LINK_KEY, URL)
                    context.startIntentIfResolves(receivedIntent)
                }
                else -> {
//                    when(UserPreferences.restrictions.subscriptionStatus) {
//                        SubscriptionStatus.MISSING, SubscriptionStatus.CANCELLED, SubscriptionStatus.CANCELLED_PAST_DUE,
//                            SubscriptionStatus.EXPIRED, S
//                    }
                    val receivedIntent = Intent(context, HomeActivity::class.java)
                    receivedIntent.putExtra(Constants.APPBOY_DEEP_LINK_KEY, URL)
                    context.startIntentIfResolves(receivedIntent)
                }
            }
        }
    }
}
