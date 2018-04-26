package com.mobile.responses;

import com.mobile.model.User;

public class AndroidIDVerificationResponse {

    String email;
    String password;
    String deviceId;
    String authToken;
    int userId;

    public String getOne_device_id() {
        return one_device_id;
    }

    String one_device_id;
    String deviceType;

    public User getUser() {
        return user;
    }

    User user;

    public AndroidIDVerificationResponse( String deviceType, String deviceId, int userID, String device) {
        this.deviceId = deviceId;
        this.userId = userID;
        this.deviceType = deviceType;
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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getAndroidID() {
        return deviceId;
    }

    public void setAndroidID(String device_id) {
        this.deviceId = device_id;
    }
}
