package com.mobile.deeplinks

import com.mobile.model.Movie

interface DeepLinksManager {

    fun determineCategory(url: String?)
    fun retrieveMovieObjectFromDeepLink(): Movie?
}