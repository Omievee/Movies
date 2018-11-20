package com.mobile.fragments

import com.mobile.model.Movie
import com.mobile.responses.CurrentMoviesResponse

interface MoviesView {
    fun hideProgress()
    fun showProgress()
    fun updateAdapter(t1: CurrentMoviesResponse)
    fun showSubscriptionActivationRequired()
    fun showDeepLinkMovie(movie: Movie?)
    fun hideSubscriptionActivationRequired()
}