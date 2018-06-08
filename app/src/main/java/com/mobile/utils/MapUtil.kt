package com.mobile.utils

import android.content.Intent
import android.net.Uri

class MapUtil {
    companion object {

        fun mapIntent(lat:Number, lng:Number): Intent {
            return Intent(Intent.ACTION_VIEW, Uri.parse("geo:${lat},${lng}?z=15")).apply {
                `package` = "com.google.android.apps.maps"
            }
        }

    }
}