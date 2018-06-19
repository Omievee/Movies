package com.mobile.home

import com.mobile.responses.MicroServiceRestrictionsResponse
import com.mobile.rx.Schedulers
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class RestrictionsManager {

    val payloadSub: BehaviorSubject<MicroServiceRestrictionsResponse> = BehaviorSubject.create()

    fun payload(): Observable<MicroServiceRestrictionsResponse> {
        return payloadSub.compose(Schedulers.observableDefault())
    }

    fun publish(it: MicroServiceRestrictionsResponse) {
        payloadSub.onNext(it)
    }
}
