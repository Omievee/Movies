package com.mobile.requests;

public class LogInRequest {

    String email;
    String password;
    String deviceId;
    String deviceType;
    String device;

    public LogInRequest(String email, String password, String deviceId, String deviceType, String device) {
        this.email = email;
        this.password = password;
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.device = device;
    }
}
