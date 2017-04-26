package com.moviepass.model;

import java.util.List;

public class User {

    int id;
    String firstName;
    String lastName;
    String email;
    String authToken;
    String deviceUuid;
    List<String> entitlements;

    public User() {
    }

    public int getId() {
        return id;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }
}