package com.mobile.fragments

import android.location.Location
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import com.google.android.gms.ads.formats.NativeCustomTemplateAd
import com.mobile.application.Application
import com.mobile.deeplinks.DeepLinksManager
import com.mobile.home.RestrictionsManager
import com.mobile.location.LocationManager
import com.mobile.model.Movie
import com.mobile.movie.MoviesManager
import com.mobile.responses.CurrentMoviesResponse
import com.moviepass.R
import io.reactivex.disposables.Disposable

class MoviesFragmentPresenter(val context: Application, val view: MoviesView, val movieManager: MoviesManager, val restrictionsManager: RestrictionsManager, val deepLinksManager: DeepLinksManager, val locationManager: LocationManager) {

    var moviesDisposable: Disposable? = null
    var restrictionsDisposable: Disposable? = null
    var movie: Movie? = null
    var deeplinksDisposable: Disposable? = null
    var locationDisposable: Disposable? = null
    var ad: NativeCustomTemplateAd? = null
    var t1: CurrentMoviesResponse? = null
    var currentLocation: Location? = null
    fun onViewCreated() {
        determineMovieFromDeepLink()
    }


    fun onResume() {

        moviesDisposable?.dispose()
        view.showProgress()
        moviesDisposable = movieManager
                .getCurrentMovies()
                .doAfterTerminate {
                    view.hideProgress()
                }
                .subscribe { t1, t2 ->
                    t1 ?: return@subscribe
                    this.t1 = t1
                    checkForNativeAd()
                }
        restrictionsDisposable?.dispose()
        restrictionsDisposable = restrictionsManager
                .payload()
                .subscribe({ res ->
                    if (res.subscriptionActivationRequired) {
                        view.showSubscriptionActivationRequired()
                    } else {
                        view.hideSubscriptionActivationRequired()
                    }
                }, {
                    it.printStackTrace()
                })

    }

    fun onDestroy() {
        moviesDisposable?.dispose()
        restrictionsDisposable?.dispose()
        deeplinksDisposable?.dispose()
    }

    fun onRefresh() {
        onResume()
    }

    fun determineMovieFromDeepLink() {
        deeplinksDisposable?.dispose()
        deeplinksDisposable = deepLinksManager
                .subScribeToDeepLink()
                .doFinally { movie = null }
                .map {
                    movie = it.movie
                }
                .subscribe({
                    if (movie != null) {
                        view.showDeepLinkMovie(movie)
                    }
                }, {
                    it.printStackTrace()
                })

    }


    private fun checkForNativeAd() {
        val adLoader = AdLoader.Builder(context, context.getString(R.string.ad_manager_hero_unit))
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(errorCode: Int) {
                        ad = null
                        view.updateAdapter(t1, null)
                    }
                })
                .forCustomTemplateAd(context.getString(R.string.ad_manager_native_template_id), {
                    it.recordImpression()
                    ad = it

                    if (ad != null) {
                        view.updateAdapter(t1, ad)
                    } else {
                        view.updateAdapter(t1, null)
                    }

                },
                        { nativeCustomTemplateAd, s ->
                            nativeCustomTemplateAd.performClick("")
                        })
                .build()

        adLoader
                .loadAd(
                        PublisherAdRequest
                                .Builder()
                                .setLocation(userCurrentLocation())
                                .build())

    }

    fun userCurrentLocation(): Location? {

        locationDisposable?.dispose()
        locationDisposable = locationManager
                .location()
                .subscribe({
                    currentLocation = it.toLocation()
                }, {
                    it.printStackTrace()
                })

        return currentLocation
    }

    fun onAdIdFound(adId: Int) {
        moviesDisposable?.dispose()
        moviesDisposable = movieManager
                .getAllMovies()
                .map { m ->
                    m.find {
                        it.id == adId
                    }
                }
                .subscribe({
                    view.showDeepLinkMovie(it)
                }, {
                    it.printStackTrace()
                })
    }

}
