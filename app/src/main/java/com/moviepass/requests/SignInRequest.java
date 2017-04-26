package com.moviepass.requests;

public class SignInRequest {

    String email;
    String password;

    public SignInRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
