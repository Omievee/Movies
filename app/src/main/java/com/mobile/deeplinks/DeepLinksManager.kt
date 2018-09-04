package com.mobile.deeplinks

import android.content.Intent

interface DeepLinksManager {

    fun onMovieIntent(intent: Intent)
    fun onTheaterIntent(intent: Intent)

}