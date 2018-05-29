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
package com.mobile.rx;

import com.google.gson.Gson;
import com.mobile.ApiError;

import java.io.InputStreamReader;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.plugins.RxJavaPlugins;
import retrofit2.HttpException;
import retrofit2.Response;

final class BodyObservable<T> extends Observable<T> {
    private final Observable<Response<T>> upstream;
    private final Gson errorGson;

    BodyObservable(Gson errorGson, Observable<Response<T>> upstream) {
        this.errorGson = errorGson;
        this.upstream = upstream;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        upstream.subscribe(new BodyObserver<T>(errorGson, observer));
    }

    private static class BodyObserver<R> implements Observer<Response<R>> {
        private final Observer<? super R> observer;
        private final Gson errorGson;
        private boolean terminated;

        BodyObserver(Gson errorGson, Observer<? super R> observer) {
            this.observer = observer;
            this.errorGson = errorGson;
        }

        @Override
        public void onSubscribe(Disposable disposable) {
            observer.onSubscribe(disposable);
        }

        @Override
        public void onNext(Response<R> response) {
            if (response.isSuccessful()) {
                observer.onNext(response.body());
            } else {
                Throwable throwable = null;
                if (response.code() / 100 == 4) {
                    try {
                        com.mobile.Error error = errorGson.fromJson(new InputStreamReader(response.errorBody().byteStream()), com.mobile.Error.class);
                        terminated = true;
                        throwable = new ApiError(error, response.code());
                    } catch (Exception e) {
                        throwable = new HttpException(response);
                        e.printStackTrace();
                    }
                } else {
                    terminated = true;
                    throwable = new HttpException(response);
                }
                try {
                    observer.onError(throwable);
                } catch (Throwable inner) {
                    Exceptions.throwIfFatal(inner);
                    RxJavaPlugins.onError(new CompositeException(throwable, inner));
                }
            }
        }

        @Override
        public void onComplete() {
            if (!terminated) {
                observer.onComplete();
            }
        }

        @Override
        public void onError(Throwable throwable) {
            if (!terminated) {
                observer.onError(throwable);
            } else {
                // This should never happen! onNext handles and forwards errors automatically.
                Throwable broken = new AssertionError(
                        "This should never happen! Report as a bug with the full stacktrace.");
                //noinspection UnnecessaryInitCause Two-arg AssertionError constructor is 1.7+ only.
                broken.initCause(throwable);
                RxJavaPlugins.onError(broken);
            }
        }
    }
}
