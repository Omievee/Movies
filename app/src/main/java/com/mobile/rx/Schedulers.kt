package com.mobile.rx

import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class Schedulers {

    companion object {
        private var SINGLE_DEFAULT: DefaultTransformer<*> = DefaultTransformer<Any>()

        private var SINGLE_BACKGROUND: SingleBackgroundTransformer<*> = SingleBackgroundTransformer<Any>()

        private var OBSERVABLE_DEFAULT: ObservableDefault<*> = ObservableDefault<Any>()

        fun <T> singleDefault() : DefaultTransformer<T> {
            @Suppress("UNCHECKED_CAST")
            return SINGLE_DEFAULT as DefaultTransformer<T>
        }

        fun <T> singleBackground() : DefaultTransformer<T> {
            @Suppress("UNCHECKED_CAST")
            return SINGLE_DEFAULT as DefaultTransformer<T>
        }

        fun <T> observableDefault() : ObservableDefault<T> {
            @Suppress("UNCHECKED_CAST")
            return OBSERVABLE_DEFAULT as ObservableDefault<T>
        }
    }

    class DefaultTransformer<T> : SingleTransformer<T, T> {
        override fun apply(upstream: Single<T>): SingleSource<T> {
            return upstream.subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
        }
    }

    class SingleBackgroundTransformer<T> : SingleTransformer<T, T> {
        override fun apply(upstream: Single<T>): SingleSource<T> {
            return upstream.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    class ObservableDefault<T> : ObservableTransformer<T, T> {
        override fun apply(upstream: Observable<T>): ObservableSource<T> {
            return upstream.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }
}