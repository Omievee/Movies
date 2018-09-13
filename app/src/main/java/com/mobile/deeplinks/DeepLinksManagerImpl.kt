package com.mobile.deeplinks

import com.mobile.application.Application
import com.mobile.model.Movie
import com.mobile.model.Theater
import com.mobile.movie.MoviesManager
import com.mobile.rx.Schedulers
import com.mobile.theater.TheaterManager
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

class DeepLinksManagerImpl(val context: Application, val moviesManager: MoviesManager, val theatersManager: TheaterManager) : DeepLinksManager {


    lateinit var moviesList: List<Movie>
    var moviesSub: Disposable? = null
    var movieObject: Movie? = null
    var theaterObject: Theater? = null
    private val subject: BehaviorSubject<Theater> = BehaviorSubject.create()

    override fun determineCategory(url: String?) {
        val type = url?.split("/".toRegex())
        try {
            when (type?.get(4)) {
                "movies" -> {
                    val movieID = type[5]
                    findCorrespondingMovie(Integer.valueOf(movieID))
                }
                "theaters" -> {
                    val theaterID = type[5]
                    findCorrespondingTheater(Integer.valueOf(theaterID))
                }
            }
        } catch (e: ArrayIndexOutOfBoundsException) {

        }
    }

    private fun findCorrespondingMovie(movieId: Int) {
        moviesSub?.dispose()
        moviesSub = moviesManager
                .getAllMovies()
                .map { m ->
                    moviesList = m
                }
                .subscribe({ _ ->
                    for (i in 0 until moviesList.size) {
                        if (moviesList[i].id == movieId) {
                            movieObject = moviesList[i]
                        }
                    }
                }, {
                    it.printStackTrace()
                })
    }

    override fun retrieveMovieObjectFromDeepLink(): Movie? {
        return this.movieObject
    }

    private fun findCorrespondingTheater(theaterId: Int) {
        val theater = theatersManager.theaterFromDeepLink(theaterId)
        theaterObject = theater
    }

    override fun subScribeToTheaterDeepLink(): Observable<Theater> {
        return subject.compose(Schedulers.observableDefault())
    }

    override fun retrieveTheaterFromDeepLink(): Theater? {
        return this.theaterObject
    }



}