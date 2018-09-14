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
    var theatersList: List<Theater>? = null

    override fun handleCategory(url: String?) {
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>handeling...")
        val movieOrTheaterId = url?.split("/")?.last()?.toString() ?: ""
        try {
            val type = url?.split("/".toRegex())
            println(">>>>>>>>>>>>>>>>>>>>>>> TYPE" + type?.get(4))
            when (type?.get(4)) {
                "movies" -> {
                    println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>movies type")
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
                .theaters(theaterId)
                .map { t ->
                    theatersList = t
                }
                .subscribe({
                    //    subject.onNext(DeepLinkCategory(null, ))
                }, {
                    it.printStackTrace()
                })
    }

    private fun findCorrespondingMovie(movieId: Int) {
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>movies search")
        moviesSub?.dispose()
        moviesSub = moviesManager
                .getAllMovies()
                .map { m ->
                    m.find {

                        it.id == movieId
                    }
                }
                .subscribe({
                    println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>movies found" + it?.title)
                    subject.onNext(DeepLinkCategory(it, null))
                    println(subject.value)
                }, {
                    it.printStackTrace()
                })
    }

    override fun subScribeToDeepLink(): Observable<DeepLinkCategory> {
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>movies subscribe")
        return subject.compose(Schedulers.observableDefault())
    }
}

