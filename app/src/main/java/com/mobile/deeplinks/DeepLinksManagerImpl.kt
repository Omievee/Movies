package com.mobile.deeplinks

import com.mobile.application.Application
import com.mobile.model.Theater
import com.mobile.movie.MoviesManager
import com.mobile.rx.Schedulers
import com.mobile.theater.TheaterManager
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

class DeepLinksManagerImpl(val context: Application, val moviesManager: MoviesManager, val theatersManager: TheaterManager) : DeepLinksManager {


    var moviesSub: Disposable? = null
    var theaterSub: Disposable? = null
    val subject: BehaviorSubject<DeepLinkCategory> = BehaviorSubject.create()
    var theater: Theater? = null

    override fun handleCategory(url: String?) {
        val movieOrTheaterId = url?.split("/")?.last()?.toString() ?: ""
        try {
            val type = url?.split("/".toRegex())
            when (type?.get(4)) {
                "movies" -> {
                    findCorrespondingMovie(Integer.valueOf(movieOrTheaterId))
                }
                "theaters" -> {
                    findCorrespondingTheater(Integer.valueOf(movieOrTheaterId))
                }
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }

    private fun findCorrespondingTheater(theaterId: Int) {
        theaterSub?.dispose()
        theaterSub = theatersManager
                .theaterDeepLink(theaterId)
                .compose(Schedulers.observableDefault())
                .map {
                    theater = it.theater
                }
                .doFinally { theater = null }
                .subscribe({
                    subject.onNext(DeepLinkCategory(null, theater))
                }, {
                    it.printStackTrace()
                })
    }


    private fun findCorrespondingMovie(movieId: Int) {
        moviesSub?.dispose()
        moviesSub = moviesManager
                .getAllMovies()
                .map { m ->
                    m.find {
                        it.id == movieId
                    }
                }
                .subscribe({
                    subject.onNext(DeepLinkCategory(it, null))
                }, {
                    it.printStackTrace()
                })
    }

    override fun subScribeToDeepLink(): Observable<DeepLinkCategory> {
        return subject.compose(Schedulers.observableDefault())
    }
}

