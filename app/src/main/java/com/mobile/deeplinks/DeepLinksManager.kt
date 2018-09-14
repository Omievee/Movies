package com.mobile.deeplinks

import com.mobile.model.Movie
import com.mobile.model.Theater
import io.reactivex.Observable

interface DeepLinksManager {

    fun handleCategory(url: String?)
    fun subScribeToDeepLink(): Observable<DeepLinkCategory>
}

class DeepLinkCategory(val movie: Movie? = null, val theater: Theater? = null)