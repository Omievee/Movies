package com.mobile.ticketverification

import com.mobile.rx.Schedulers
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

class DetectedTextManager {

    private val subject:PublishSubject<TextBlock> = PublishSubject.create()

    fun broadcast(block: TextBlock) {
        subject.onNext(block)
    }
    fun payload(): Observable<TextBlock> {
        return subject
                .compose(Schedulers.observableDefault())
    }
}