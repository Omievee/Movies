package com.mobile.fragments

import com.mobile.movie.MoviesManager
import io.reactivex.disposables.Disposable

class MoviesFragmentPresenter(val view:MoviesView, val movieManager:MoviesManager) {

    var moviesDisposable:Disposable?=null

    fun onViewCreated() {
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
                    t1?: return@subscribe
                    view.updateAdapter(t1)
                }
    }

    fun onDestroy() {
        moviesDisposable?.dispose()
    }

    fun onRefresh() {
        onResume()
    }
}
