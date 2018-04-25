package com.mobile.responses;

public class AndroidIDVerificationResponse {

    String email;
    String password;
    String device_id;

    public String getOne_device_id() {
        return one_device_id;
    }

    String one_device_id;
    String device_type;

    public AndroidIDVerificationResponse(String email, String password, String device_id, String device_type, String device) {
        this.email = email;
        this.password = password;
        this.device_id = device_id;
        this.device_type = device_type;
        this.device = device;
    }

    String device;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getDevice_type() {
        return device_type;
    }

    public void setDevice_type(String device_type) {
        this.device_type = device_type;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getAndroidID() {
        return device_id;
    }

    public void setAndroidID(String device_id) {
        this.device_id = device_id;
    }
}
