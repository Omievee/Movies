package com.mobile.rx

import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class Schedulers {

    companion object {
        private var SINGLE_BACKGROUND: DefaultTransformer<*> = DefaultTransformer<Any>()

        private var SINGLE_DEFAULT: SingleDefaultTransformer<*> = SingleDefaultTransformer<Any>()

        private var OBSERVABLE_DEFAULT: ObservableDefault<*> = ObservableDefault<Any>()

        private var OBSERVABLE_BACKGROUND: ObservableBackground<*> = ObservableBackground<Any>()

        fun <T> singleDefault() : SingleDefaultTransformer<T> {
            @Suppress("UNCHECKED_CAST")
            return SINGLE_DEFAULT as SingleDefaultTransformer<T>
        }

        fun <T> singleBackground() : DefaultTransformer<T> {
            @Suppress("UNCHECKED_CAST")
            return SINGLE_BACKGROUND as DefaultTransformer<T>
        }

        fun <T> observableDefault() : ObservableDefault<T> {
            @Suppress("UNCHECKED_CAST")
            return OBSERVABLE_DEFAULT as ObservableDefault<T>
        }

        fun <T> observableBackground() : ObservableBackground<T> {
            @Suppress("UNCHECKED_CAST")
            return OBSERVABLE_BACKGROUND as ObservableBackground<T>
        }
    }

    class DefaultTransformer<T> : SingleTransformer<T, T> {
        override fun apply(upstream: Single<T>): SingleSource<T> {
            return upstream.subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
        }
    }

    class SingleDefaultTransformer<T> : SingleTransformer<T, T> {
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

    class ObservableBackground<T> : ObservableTransformer<T, T> {
        override fun apply(upstream: Observable<T>): ObservableSource<T> {
            return upstream.subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
        }
    }
}