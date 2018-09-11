//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.mobile.rx;

import com.google.gson.Gson;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import java.lang.reflect.Type;

import io.reactivex.annotations.Nullable;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;

public class RxJava2CallAdapter<R> implements CallAdapter<R, Object> {
    private final Type responseType;
    @Nullable
    private final Scheduler scheduler;
    private final boolean isAsync;
    private final boolean isResult;
    private final boolean isBody;
    private final boolean isFlowable;
    private final boolean isSingle;
    private final boolean isMaybe;
    private final boolean isCompletable;
    private final Gson errorGson;

    RxJava2CallAdapter(Gson errorGson, Type responseType, @Nullable Scheduler scheduler, boolean isAsync, boolean isResult, boolean isBody, boolean isFlowable, boolean isSingle, boolean isMaybe, boolean isCompletable) {
        this.errorGson = errorGson;
        this.responseType = responseType;
        this.scheduler = scheduler;
        this.isAsync = isAsync;
        this.isResult = isResult;
        this.isBody = isBody;
        this.isFlowable = isFlowable;
        this.isSingle = isSingle;
        this.isMaybe = isMaybe;
        this.isCompletable = isCompletable;
    }

    public Type responseType() {
        return this.responseType;
    }

    public Object adapt(Call<R> call) {
        Observable<Response<R>> responseObservable = this.isAsync ? new CallEnqueueObservable(call) : new CallExecuteObservable(call);
        Object observable;
        if (this.isResult) {
            observable = new ResultObservable((Observable)responseObservable);
        } else if (this.isBody) {
            observable = new BodyObservable(errorGson, (Observable)responseObservable);
        } else {
            observable = responseObservable;
        }

        observable = ((Observable)observable).compose(Schedulers.Companion.observableDefault());

        if (this.isFlowable) {
            return ((Observable)observable).toFlowable(BackpressureStrategy.LATEST);
        } else if (this.isSingle) {
            return ((Observable)observable).singleOrError();
        } else if (this.isMaybe) {
            return ((Observable)observable).singleElement();
        } else {
            return this.isCompletable ? ((Observable)observable).ignoreElements() : observable;
        }
    }
}
