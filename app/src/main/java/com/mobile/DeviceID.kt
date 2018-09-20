package com.mobile

import android.content.Context
import android.provider.Settings

object DeviceID {

    fun getID(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}