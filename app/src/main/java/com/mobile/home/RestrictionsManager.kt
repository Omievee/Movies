package com.mobile.home

import com.mobile.responses.RestrictionsResponse
import com.mobile.rx.Schedulers
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class RestrictionsManager {

    val payloadSub: BehaviorSubject<RestrictionsResponse> = BehaviorSubject.create()

    fun payload(): Observable<RestrictionsResponse> {
        return payloadSub.compose(Schedulers.observableDefault())
    }

    fun publish(it: RestrictionsResponse) {
        payloadSub.onNext(it)
    }
}
