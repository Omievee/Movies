package com.mobile.history

import com.mobile.UserPreferences
import com.mobile.UserPreferences.historyLoadedDate
import com.mobile.db.ReservationDao
import com.mobile.UserPreferences.saveHistoryLoadedDate
import com.mobile.history.StarsRating.Rating
import com.mobile.history.model.ReservationHistory
import com.mobile.network.Api
import com.mobile.network.GwApi
import com.mobile.requests.RatingRequest
import com.mobile.rx.Schedulers
import com.mobile.session.SessionManager
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.util.*
import javax.inject.Provider

class HistoryManagerImpl(@History val dao: Provider<ReservationDao>, val api: GwApi, val sessionManager: SessionManager) : HistoryManager {


    var getHistoryFromApi: Disposable? = null


    init {
        sessionManager.loggedOut()
                .map {
                    dao.get().deleteAll()
                }
                .subscribe {

                }
    }

    private fun getHistoryInternal(onlyFromWeb: Boolean = false): Observable<List<ReservationHistory>> {
        return Observable.create<List<ReservationHistory>> {

            if (!onlyFromWeb) {
                val movies = dao.get().getHistory().sortedByDescending { v-> v.created }

                if (it.isDisposed) {
                    return@create
                }

                it.onNext(movies)
            }

//            if (hasItBeenFourHoursSinceHistoryTimeStamp) {
                getHistoryFromApi(it)
//            } else {
//                if (!it.isDisposed) {
//                    it.onComplete()
//                }
            //}
        }.compose(Schedulers.observableDefault())
    }

    override fun getHistory(): Observable<List<ReservationHistory>> {
        return getHistoryInternal(false)
    }

    val hasItBeenFourHoursSinceHistoryTimeStamp: Boolean
        get() {
            val fourHoursInPast = Calendar.getInstance().apply {
                add(Calendar.HOUR, -4)
            }
            return historyLoadedDate.before(fourHoursInPast)

        }

    private fun getHistoryFromApi(emitter: ObservableEmitter<List<ReservationHistory>>) {
        getHistoryFromApi?.dispose()
        getHistoryFromApi =
                api.getReservationHistory()
                        .compose(Schedulers.singleBackground())
                        .map { reservationHistoryResponse ->

                            Collections.sort<ReservationHistory>(reservationHistoryResponse.reservations) { o1, o2 ->
                                val latestReservation = o2.created
                                val oldestReservation = o1.created

                                latestReservation.compareTo(oldestReservation)
                            }

                            reservationHistoryResponse.reservations?.let {
                                 dao.get().replaceHistory(it)
                                saveHistoryLoadedDate()
                                it
                            } ?: throw RuntimeException()
                        }.map {
                            val lastMovie = it.firstOrNull()
                            lastMovie?.let { reservation ->
                                UserPreferences
                                        .setLastMovieSeen(reservation)
                            }
                            UserPreferences
                                    .setTotalMoviesSeen(it.size)
                            val count = it.count { it.showtimeDate?.after(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }.time) == true }
                            UserPreferences.setTotalMoviesSeenLast30Days(count)
                            it
                        }
                        .doAfterTerminate {
                            if (emitter.isDisposed) {
                                return@doAfterTerminate
                            }
                            emitter.onComplete()
                        }
                        .subscribe { success, error ->
                            if (emitter.isDisposed) {
                                return@subscribe
                            }
                            error?.let {
                                emitter.onError(it)
                            } ?: emitter.onNext(success)
                        }
    }

    override fun submitRatingV2(history: ReservationHistory, rating: Rating): Single<ReservationHistory> {
        val id = history.id ?: 0

        val userRating = rating.starRating
        return api
                .submitRatingV2(RatingRequest(id,userRating))
                .compose(Schedulers.singleBackground())
                .map { _ ->
                    history.userRating = userRating
                    dao.get().update(history)
                    history
                }
                .compose(Schedulers.singleDefault())
    }

    override fun fetchLastMovieWithoutRating(): Single<ReservationHistory> {
        return getHistoryInternal(true)
                .map {
                    it.firstOrNull()
                }
                .map {
                    if (it.userRating != null) {
                        throw IllegalArgumentException("Last movie has been rated")

                    }
                    it
                }.singleOrError()
    }
}