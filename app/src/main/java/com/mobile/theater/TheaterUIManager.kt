package com.mobile.theater

import com.mobile.location.UserLocation
import com.mobile.model.Theater
import com.mobile.rx.Schedulers
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class TheaterUIManager {

    private var mappedTheaters: PublishSubject<TheatersPayload> = PublishSubject.create()
    private var listTheaters: BehaviorSubject<TheatersPayload> = BehaviorSubject.create()


    fun mappedTheaters(): Observable<TheatersPayload> {
        return mappedTheaters.compose(Schedulers.observableDefault())
    }

    fun mappedTheaters(theaters: TheatersPayload) {
        mappedTheaters.onNext(theaters)
    }

    fun listTheaters(theaters:TheatersPayload) {
        listTheaters.onNext(theaters)
    }

    fun listTheaters():Observable<TheatersPayload> {
        return listTheaters.compose(Schedulers.observableDefault())
    }

    fun listTheatersLocation():UserLocation? {
        return listTheaters.value?.location
    }

    fun cleanup() {
        mappedTheaters.onComplete()
        mappedTheaters = PublishSubject.create()
    }

}

class TheatersPayload(val location:UserLocation, val theaters:List<Theater>)
