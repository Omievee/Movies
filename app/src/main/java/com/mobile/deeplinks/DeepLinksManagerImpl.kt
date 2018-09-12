package com.mobile.deeplinks

import com.mobile.application.Application
import com.mobile.model.Movie
import com.mobile.movie.MoviesManager
import io.reactivex.disposables.Disposable

class DeepLinksManagerImpl(val context: Application, val moviesManager: MoviesManager) : DeepLinksManager {


    var moviesSub: Disposable? = null
    lateinit var moviesList: List<Movie>
    var movieObject: Movie? = null

    override fun determineCategory(url: String?) {
        url ?: return
        val type = url.split("/".toRegex())
        when (type[4]) {
            "movies" -> {
                val movieID = type[5]
                findCorrespondingMovie(Integer.valueOf(movieID))
            }
            "theaters" -> {
                val theaterID = type[5]
                findCorrespondingTheater(Integer.valueOf(theaterID))
            }
        }
    }


    private fun findCorrespondingTheater(theaterId: Int) {
        //TODO : Same for theaters...
    }

    private fun findCorrespondingMovie(movieId: Int) {
        moviesSub?.dispose()
        moviesSub = moviesManager
                .getAllMovies()
                .map { m ->
                    moviesList = m
                    for (i in 0 until moviesList.size) {
                        if (moviesList[i].id == movieId) {
                            movieObject = moviesList[i]
                        }
                    }
                }
                .subscribe({ _ ->

                }, {
                    it.printStackTrace()
                })
    }

    override fun retrieveMovieObjectFromDeepLink(): Movie? {
        return this.movieObject
    }
}