package com.moviepass.requests;

/**
 * Created by anubis on 8/2/17.
 */

public class FacebookSignInRequest {

    String facebook_token;

    public FacebookSignInRequest(String facebook_token) {
        this.facebook_token = facebook_token;
    }
}
