package com.mobile.requests;

public class LogInRequest {

    String email;
    String password;
    String device_id;
    String device_type;
    String device;

    public LogInRequest(String email, String password, String device_id, String device_type, String device) {
        this.email = email;
        this.password = password;
        this.device_id = device_id;
        this.device_type = device_type;
        this.device = device;
    }
}
