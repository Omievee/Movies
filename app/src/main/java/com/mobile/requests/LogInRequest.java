package com.mobile.requests;

public class LogInRequest {

    String email;
    String password;
    String androidID;

    public LogInRequest(String email, String password, String androidID) {
        this.email = email;
        this.password = password;
        this.androidID = androidID;
    }
}
