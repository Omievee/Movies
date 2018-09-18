/*
 * Copyright (C) 2016 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mobile.rx

import com.google.gson.Gson
import com.mobile.ApiError

import java.io.InputStreamReader

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.exceptions.Exceptions
import io.reactivex.plugins.RxJavaPlugins
import retrofit2.HttpException
import retrofit2.Response

internal class BodyObservable<T>(private val errorGson: Gson, private val upstream: Observable<Response<T>>) : Observable<T>() {

    override fun subscribeActual(observer: Observer<in T>) {
        upstream.subscribe(BodyObserver(errorGson, observer))
    }

    private class BodyObserver<R> internal constructor(private val errorGson: Gson, private val observer: Observer<in R>) : Observer<Response<R>> {
        private var terminated: Boolean = false

        override fun onSubscribe(disposable: Disposable) {
            observer.onSubscribe(disposable)
        }

        override fun onNext(response: Response<R>) {
            if (response.isSuccessful) {
                observer.onNext(response.body()!!)
            } else {
                var throwable: Throwable? = null
                if (response.code() / 100 == 4) {
                    try {
                        val error = errorGson.fromJson(InputStreamReader(response.errorBody()!!.byteStream()), com.mobile.Error::class.java)
                        terminated = true
                        throwable = when(response.code()) {
                            412-> PendingChargesError(error, response.code())
                            else-> ApiError(error, response.code())
                        }
                    } catch (e: Exception) {
                        throwable = HttpException(response)
                        e.printStackTrace()
                    }

                } else {
                    terminated = true
                    throwable = HttpException(response)
                }
                try {
                    observer.onError(throwable!!)
                } catch (inner: Throwable) {
                    Exceptions.throwIfFatal(inner)
                    RxJavaPlugins.onError(CompositeException(throwable, inner))
                }

            }
        }

        override fun onComplete() {
            if (!terminated) {
                observer.onComplete()
            }
        }

        override fun onError(throwable: Throwable) {
            if (!terminated) {
                observer.onError(throwable)
            } else {
                // This should never happen! onNext handles and forwards errors automatically.
                val broken = AssertionError(
                        "This should never happen! Report as a bug with the full stacktrace.")

                broken.initCause(throwable)
                RxJavaPlugins.onError(broken)
            }
        }
    }
}