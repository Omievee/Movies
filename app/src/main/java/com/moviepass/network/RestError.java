package com.moviepass.network;

import org.parceler.Parcel;

@Parcel
public class RestError {

    String message;

    public RestError() {
    }

    public RestError(String strMessage) {
        message = strMessage;
    }

    public String getMessage() {
        return message;
    }
}