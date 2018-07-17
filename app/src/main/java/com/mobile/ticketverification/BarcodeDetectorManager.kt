package com.mobile.ticketverification

import com.mobile.camera.BarcodeData
import com.mobile.rx.Schedulers
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class BarcodeDetectorManager() {

    val publishSubject: PublishSubject<BarcodeData> = PublishSubject.create()

    fun broadcast(block: BarcodeData) {
        publishSubject.onNext(block)
    }

    fun payload(): Observable<BarcodeData> {
        return publishSubject.compose(Schedulers.observableDefault())
    }

}