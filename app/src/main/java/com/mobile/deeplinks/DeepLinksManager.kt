package com.mobile.deeplinks

import com.mobile.model.Movie
import com.mobile.model.Theater

interface DeepLinksManager {

    fun determineCategory(url: String?)
    fun retrieveMovieObjectFromDeepLink(): Movie?
    fun retrieveTheaterObjectFromDeepLink(): Theater?
}