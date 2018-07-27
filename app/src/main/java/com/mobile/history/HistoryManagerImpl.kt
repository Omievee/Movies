package com.mobile.history

import com.mobile.UserPreferences
import com.mobile.UserPreferences.saveHistoryLoadedDate
import com.mobile.UserPreferences.historyLoadedDate
import com.mobile.history.model.ReservationHistory
import com.mobile.network.Api
import com.mobile.responses.HistoryResponse
import com.mobile.rx.Schedulers
import com.mobile.utils.DateUtils
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.realm.Realm
import java.util.*
import javax.inject.Provider

class HistoryManagerImpl(@History val realmHistory: Provider<Realm>, val api: Api) : HistoryManager {

    var getHistoryFromApi: Disposable? = null

    override fun getHistory(): Observable<List<ReservationHistory>> {
        return Observable.create<List<ReservationHistory>> {

            val movies = realmHistory.get()
                    .where(ReservationHistory::class.java)
                    .findAll()

            val wasHistoryUpdatedToday = movies
                    .find {
                        DateUtils.everyFourHours(it.updatedAt)
                    } != null
            if (it.isDisposed) {
                return@create
            }

            it.onNext(realmHistory.get().copyFromRealm(movies))

            if (!wasHistoryUpdatedToday || hasItBeenFourHoursSinceHistoryTimeStamp) {
                getHistoryFromApi(it)
            } else {
                if (!it.isDisposed) {
                    it.onComplete()
                }
            }
        }.compose(Schedulers.observableDefault())
    }

    val hasItBeenFourHoursSinceHistoryTimeStamp: Boolean
    get() {
        val lastSavedHistory = historyLoadedDate.add(Calendar.HOUR,4)
        return Calendar.getInstance().after(lastSavedHistory)
    }

    private fun getHistoryFromApi(emitter: ObservableEmitter<List<ReservationHistory>>) {
        getHistoryFromApi?.dispose()
        getHistoryFromApi =
                api.reservationHistory
                        .compose(Schedulers.singleBackground())
                        .map { reservationHistoryResponse ->

                            Collections.sort<ReservationHistory>(reservationHistoryResponse.reservations) { o1, o2 ->
                                val latestReservation = o2.createdAt ?: 0
                                val oldestReservation = o1.createdAt ?: 0

                                latestReservation.compareTo(oldestReservation)
                            }

                            reservationHistoryResponse.reservations?.let {
                                realmHistory.get().executeTransaction { transaction ->
                                    transaction.insertOrUpdate(it)
                                    saveHistoryLoadedDate()
                                }
                                it
                            } ?: throw RuntimeException()
                        }.map {
                            val lastMovie = it.firstOrNull()
                            lastMovie?.let {
                                UserPreferences
                                        .setLastMovieSeen(lastMovie)
                            }
                            UserPreferences
                                    .setTotalMoviesSeen(it.size)
                            val count = it.count { it.created?.after(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }.time) == true }
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

    override fun submitRating(history: ReservationHistory, wasGood: Boolean): Single<ReservationHistory> {
        val id = history.id ?: 0

        val rating = when (wasGood) {
            true -> "GOOD"
            false -> "BAD"
        }
        return api
                .submitRatingRx(id, HistoryResponse(rating))
                .doOnSuccess { _ ->
                    history.userRating = rating
                    realmHistory.get().executeTransaction { r ->
                        r.insertOrUpdate(history)
                    }
                }
                .map { _ ->
                    history
                }
    }

    override fun fetchLastMovieWithoutRating(): Single<ReservationHistory> {
        return getHistory()
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