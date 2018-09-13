package com.mobile.deeplinks

import com.mobile.model.Movie
import com.mobile.model.Theater
import io.reactivex.Observable

interface DeepLinksManager {

    fun determineCategory(url: String?)
    fun retrieveMovieObjectFromDeepLink(): Movie?
    fun subScribeToTheaterDeepLink(): Observable<Theater>
    fun retrieveTheaterFromDeepLink(): Theater?
}