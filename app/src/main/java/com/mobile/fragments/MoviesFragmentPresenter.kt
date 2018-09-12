package com.mobile.fragments

import com.mobile.deeplinks.DeepLinksManager
import com.mobile.home.RestrictionsManager
import com.mobile.movie.MoviesManager
import io.reactivex.disposables.Disposable

class MoviesFragmentPresenter(val view: MoviesView, val movieManager: MoviesManager, val restrictionsManager: RestrictionsManager, val deepLinksManager: DeepLinksManager) {

    var moviesDisposable: Disposable? = null
    var restrictionsDisposable: Disposable? = null
    var deepLinksDisp: Disposable? = null

    fun onViewCreated() {}

    fun onResume() {
        moviesDisposable?.dispose()
        view.showProgress()
        moviesDisposable = movieManager
                .getCurrentMovies()
                .doAfterTerminate {
                    view.hideProgress()
                }
                .subscribe { t1, t2 ->
                    println(t1)
                    println(t2)
                    t1 ?: return@subscribe
                    view.updateAdapter(t1)
                }
        restrictionsDisposable?.dispose()
        restrictionsDisposable = restrictionsManager
                .payload()
                .subscribe({ res ->
                    if (res.subscriptionActivationRequired) {
                        view.showSubscriptionActivationRequired()
                    }
                }, {
                    it.printStackTrace()
                })

        val movie = deepLinksManager.retrieveMovieObjectFromDeepLink()
        if (movie != null) {
            view.showDeepLinkMovie(movie)
        }
    }

    fun onDestroy() {
        moviesDisposable?.dispose()
        restrictionsDisposable?.dispose()
    }

    fun onRefresh() {
        onResume()
    }
}
