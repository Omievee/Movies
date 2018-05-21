package com.mobile.utils

import android.content.Context
import android.content.Intent

fun Context?.startIntentIfResolves(intent: Intent) {
    this?.packageManager?.let {
        intent.resolveActivity(it)?.let {
            startActivity(intent)
        }
    }

}