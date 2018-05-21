package com.mobile.history

import com.mobile.history.model.ReservationHistory
import io.reactivex.Observable

interface HistoryManager {

    /**
     * @return First element emitted is from local cache, second element emitted is from the api if local cache does not exist or has expired
     */
    fun getHistory(): Observable<List<ReservationHistory>>?

}