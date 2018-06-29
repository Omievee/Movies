package com.mobile.requests;

public class ChangeEmailRequest {
    String email;
    String password;
    int userId;

    public ChangeEmailRequest(String email, String password, int userId) {
        this.email = email;
        this.password = password;
        this.userId = userId;
    }
}
