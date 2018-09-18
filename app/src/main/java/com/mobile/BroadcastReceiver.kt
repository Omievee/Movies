package com.mobile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.appboy.push.AppboyNotificationUtils
import com.mobile.activities.LogInActivity
import com.mobile.home.HomeActivity
import com.mobile.utils.startIntentIfResolves


class BroadcastReceiver : BroadcastReceiver() {

    lateinit var context: Context


    override fun onReceive(context: Context?, intent: Intent?) {
        this.context = context ?: return

        intent?.let {
            val uri = it.extras?.get("uri").toString()
            val notificationOpenedAction = context.packageName + AppboyNotificationUtils.APPBOY_NOTIFICATION_OPENED_SUFFIX
            if (notificationOpenedAction == it.action) {
                userOpenedPush(it, uri)
            }
        }
    }

    private fun userOpenedPush(intent: Intent, uri: String?) {
        val notificationOpenedAction = context.packageName + AppboyNotificationUtils.APPBOY_NOTIFICATION_OPENED_SUFFIX
        if (notificationOpenedAction == intent.action) {
            when (UserPreferences.userId) {
                0 -> {
                    val receivedIntent = Intent(context, LogInActivity::class.java)
                    receivedIntent.putExtra(Constants.APPBOY_DEEP_LINK_KEY, uri)
                    context.startIntentIfResolves(receivedIntent)
                }
                else -> {
                    val receivedIntent = HomeActivity.newIntent(context, uri?.let { Uri.parse(it) })
                    context.startIntentIfResolves(receivedIntent)
                }
            }
        }
    }
}


