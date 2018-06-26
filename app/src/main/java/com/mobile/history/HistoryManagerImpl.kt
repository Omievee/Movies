package com.mobile.history

import com.mobile.UserPreferences
import com.mobile.UserPreferences.isHistoryLoadedToday
import com.mobile.UserPreferences.saveHistoryLoadedDate
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
            val wasHistoryUpdatedEver =
                    isHistoryLoadedToday()

            val wasHistoryUpdatedToday = movies
                    .find {
                        DateUtils.isSameDay(it.updatedAt)
                    } != null
            if (it.isDisposed) {
                return@create
            }
            it.onNext(realmHistory.get().copyFromRealm(movies))
            if (!wasHistoryUpdatedEver || !wasHistoryUpdatedToday) {
                getHistoryFromApi(it)
            } else {
                if (!it.isDisposed) {
                    it.onComplete()
                }
            }
        }.compose(Schedulers.observableDefault())
    }

    private fun getHistoryFromApi(emitter: ObservableEmitter<List<ReservationHistory>>) {
        getHistoryFromApi?.dispose()
        getHistoryFromApi =
                api.reservationHistory
                        .compose(Schedulers.singleBackground())
                        .map { reservationHistoryResponse ->
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
                            val count = it.count { it.created?.after(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR,-30) }.time)==true }
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
        val id = history.id?:0

        val rating = when (wasGood) {
            true -> "GOOD"
            false -> "BAD"
        }
        return api
                .submitRatingRx(id, HistoryResponse(rating))
                .doOnSuccess { v ->
                    history.userRating = rating
                    realmHistory.get().executeTransaction { r->
                        r.insertOrUpdate(history)
                    }
                }
                .map { res ->
                    history
                }

    }
}