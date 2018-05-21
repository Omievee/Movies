package com.mobile.history

import com.mobile.UserPreferences.*
import com.mobile.history.model.ReservationHistory
import com.mobile.network.Api
import com.mobile.rx.Schedulers
import com.mobile.utils.DateUtils
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.Disposable
import io.realm.Realm

class HistoryManagerImpl(@History val realmHistory: Realm, val api: Api) : HistoryManager {

    var getHistoryFromApi: Disposable? = null

    override fun getHistory(): Observable<List<ReservationHistory>>? {
        return Observable.create<List<ReservationHistory>> {
            val movies = realmHistory
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
            it.onNext(movies)
            if (!wasHistoryUpdatedEver || !wasHistoryUpdatedToday) {
                getHistoryFromApi(it)
            }
        }
    }

    private fun getHistoryFromApi(emitter: ObservableEmitter<List<ReservationHistory>>) {
        getHistoryFromApi?.dispose()
        getHistoryFromApi =
                api.reservationHistory
                        .compose(Schedulers.singleBackground())
                        .map { reservationHistoryResponse ->
                            reservationHistoryResponse.reservations?.let {
                                realmHistory.executeTransaction { transaction ->
                                    transaction.insertOrUpdate(it)
                                    saveHistoryLoadedDate()
                                }
                                it
                            } ?: throw RuntimeException()
                        }
                        .doAfterTerminate {
                            if(emitter.isDisposed) {
                                return@doAfterTerminate
                            }
                            emitter.onComplete()
                        }
                        .subscribe { success, errror ->
                            if (emitter.isDisposed) {
                                return@subscribe
                            }
                            errror?.let {
                                emitter.onError(it)
                            } ?: emitter.onNext(success)

                        }

    }
}