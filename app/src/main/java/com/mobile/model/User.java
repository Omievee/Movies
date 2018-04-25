package com.mobile.model;

public class User {

    int id;
    String firstName;
    String lastName;
    String email;
    String authToken;

    public String getOne_device_id() {
        return one_device_id;
    }

    String one_device_id;

    public String getAndroidID() {
        return androidID;
    }

    String androidID;
//    List<String> entitlements;

    public User() {
    }

    public int getId() {
        return id;
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