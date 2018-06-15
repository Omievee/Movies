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
                if(!it.isDisposed) {
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