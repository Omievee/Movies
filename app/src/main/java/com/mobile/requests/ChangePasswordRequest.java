package com.mobile.requests;

public class ChangePasswordRequest {

    String oldPassword;
    String newPassword;
    int userId;

    public ChangePasswordRequest(String oldPassword, String newPassword, int userId) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.userId = userId;
    }
}
