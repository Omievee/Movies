package com.mobile.model;

public class LogoutInfo {

    public boolean isForceLogOut() {
        return forceLogOut;
    }

    public String getMessage() {
        return message;
    }

    boolean forceLogOut;
    String message;

    public LogoutInfo(boolean forceLogOut, String message) {
        this.forceLogOut = forceLogOut;
        this.message = message;
    }
}
