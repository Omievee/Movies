package com.mobile.fragments

import com.google.android.gms.ads.formats.NativeCustomTemplateAd
import com.mobile.model.Movie
import com.mobile.responses.CurrentMoviesResponse

interface MoviesView {
    fun hideProgress()
    fun showProgress()
    fun updateAdapter(t1: CurrentMoviesResponse?, native:NativeCustomTemplateAd?)
    fun showSubscriptionActivationRequired()
    fun showDeepLinkMovie(movie: Movie?)
    fun hideSubscriptionActivationRequired()

}