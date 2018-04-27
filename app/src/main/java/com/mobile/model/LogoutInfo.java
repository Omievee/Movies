package com.mobile.model;

public class LogoutInfo {

    public boolean isForceLogout() {
        return forceLogout;
    }

    public String getMessage() {
        return message;
    }

    boolean forceLogout;
    String message;

    public LogoutInfo(boolean forceLogOut, String message) {
        this.forceLogout = forceLogOut;
        this.message = message;
    }
}
