package com.mobile.movie

import android.content.Context
import com.google.gson.Gson
import com.mobile.UserPreferences
import com.mobile.application.Application
import com.mobile.model.Movie
import com.mobile.network.StaticApi
import com.mobile.responses.CurrentMoviesResponse
import com.mobile.rx.Schedulers
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.*

class MoviesManagerImpl(val application: Application, val gson: Gson, val api: StaticApi) : MoviesManager {

    var movieSub: Disposable? = null

    var movies:List<Movie>?=null

    val shouldDownloadCurrentMovies: Boolean
        get() {
            return true
//            return UserPreferences.currentMoviesLoaded.before(
//                    Calendar.getInstance().apply {
//                        add(Calendar.HOUR, -2)
//                    }
//            )
        }

    private fun getCurrentMoviesInternal(): Single<CurrentMoviesResponse> {
        val single: Single<CurrentMoviesResponse> = Single.create { emitter ->
            if (shouldDownloadCurrentMovies) {
                movieSub?.dispose()
                movieSub = api.getAllCurrentMovies()
                        .map {
                            val writer = OutputStreamWriter(
                                    application.openFileOutput("currentSelected.json", Context.MODE_PRIVATE)
                            )
                            gson
                                    .toJson(it,
                                            writer
                                    )
                            UserPreferences.currentMoviesLoaded = Calendar.getInstance()
                            it
                        }
                        .subscribe { t1, t2 ->
                            if (emitter.isDisposed) {
                                return@subscribe
                            }
                            t1?.let {
                                emitter.onSuccess(t1)
                            }
                            t2?.let {
                                emitter.onError(t2)
                            }

                        }
            } else {
                try {
                    val response = gson.fromJson(InputStreamReader(application.openFileInput("currentSelected.json")), CurrentMoviesResponse::class.java)
                    if (emitter.isDisposed) {
                        return@create
                    }
                    emitter.onSuccess(response)
                } catch (e: Exception) {
                    if (emitter.isDisposed) {
                        return@create
                    }
                    emitter.onError(e)
                }
            }
        }
        return single.compose(Schedulers.singleDefault())
    }

    override fun getAllMovies(): Single<List<Movie>> {
        if(movies!=null) {
            return Single.just(movies)
        }
        return api.getAllMovies().doOnSuccess {
            movies = it
        }
    }

    override fun getCurrentMovies(): Single<CurrentMoviesResponse> {
        return getCurrentMoviesInternal()
    }

}