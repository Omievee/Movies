package com.mobile.requests;

/**
 * Created by anubis on 8/2/17.
 */

public class FacebookSignInRequest {

    String facebook_token;
    String device_ID;
    String device_type;
    String device;

    public FacebookSignInRequest(String facebook_token) {
        this.facebook_token = facebook_token;
    }
}
