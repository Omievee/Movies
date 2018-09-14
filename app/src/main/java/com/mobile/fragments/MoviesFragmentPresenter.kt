package com.mobile.fragments

import com.mobile.deeplinks.DeepLinksManager
import com.mobile.home.RestrictionsManager
import com.mobile.model.Movie
import com.mobile.movie.MoviesManager
import io.reactivex.disposables.Disposable

class MoviesFragmentPresenter(val view: MoviesView, val movieManager: MoviesManager, val restrictionsManager: RestrictionsManager, val deepLinksManager: DeepLinksManager) {

    var moviesDisposable: Disposable? = null
    var restrictionsDisposable: Disposable? = null
    var movie: Movie? = null
    var deeplinksDisposable: Disposable? = null

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
    }

    fun onDestroy() {
        moviesDisposable?.dispose()
        restrictionsDisposable?.dispose()
        deeplinksDisposable?.dispose()
    }

    fun onRefresh() {
        onResume()
    }

    private fun determineMovieFromDeepLink() {
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

}
