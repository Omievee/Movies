package com.moviepass.network;


import retrofit2.Call;
import retrofit2.Callback;

public abstract class RestCallback<T> implements Callback<T> {
    public abstract void failure(RestError restError);

    @Override
    public void onFailure(Call<T> call, Throwable t){
        RestError restError = new RestError(t.getMessage());

        if (restError != null)
            failure(restError);
        else {
            failure(new RestError(t.getMessage()));
        }
    }

}