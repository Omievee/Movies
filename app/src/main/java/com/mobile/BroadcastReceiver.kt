package com.mobile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mobile.Constants.TAG
import com.mobile.splash.SplashActivity
import com.mobile.utils.startIntentIfResolves


class BroadcastReceiver : BroadcastReceiver() {

    lateinit var URL: String

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return

        intent?.let {
            URL = it.getStringExtra(Constants.APPBOY_DEEP_LINK_KEY)
        }


        Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>>: "  + URL)
        val receivedIntent = Intent(context, SplashActivity::class.java)
        receivedIntent.putExtra(Constants.APPBOY_DEEP_LINK_KEY, URL)
        context.startIntentIfResolves(receivedIntent)


//
//        when {
//            pushReceivedAction.equals(action) -> Log.d(TAG, "Received push notification.")
//            notificationOpenedAction.equals(action) -> {
//                AppboyNotificationUtils.routeUserWithNotificationOpenedIntent(context, intent);
//                Log.d(TAG, "Received push notification & Opened.");
//            }
//            notificationDeletedAction.equals(action) -> Log.d(TAG, "Received push notification deleted intent.")
//            else -> Log.d(TAG, String.format("Ignoring intent with unsupported action %s", action))
//        }

    }

    private fun launchURLFromDeepLink(intent: Intent?) {
        intent ?: return
        val extras = intent.extras

        if (extras != null && extras.containsKey(Constants.APPBOY_PUSH_RECEIVED)) {
            val createdAt = extras.getLong(Constants.APPBOY_PUSH_RECEIVED)

        }
    }

}
