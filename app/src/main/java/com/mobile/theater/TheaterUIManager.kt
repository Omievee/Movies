package com.mobile.theater

import com.mobile.location.UserLocation
import com.mobile.model.Theater
import com.mobile.rx.Schedulers
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class TheaterUIManager {

    private var subject: PublishSubject<TheatersPayload> = PublishSubject.create()

    fun theaters(): Observable<TheatersPayload> {
        return subject.compose(Schedulers.observableDefault())
    }

    fun theaters(theaters: TheatersPayload) {
        subject.onNext(theaters)
    }

    fun cleanup() {
        subject.onComplete()
        subject = PublishSubject.create()
    }

}

class TheatersPayload(val location:UserLocation, val theaters:List<Theater>)
