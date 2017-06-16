package com.moviepass.requests;

public class LogInRequest {

    String email;
    String password;

    public LogInRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
