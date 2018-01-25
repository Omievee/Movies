package com.mobile.requests;

/**
 * Created by anubis on 8/30/17.
 */

public class CredentialsRequest {

    String step = "CREDENTIALS";
    String email;

    public CredentialsRequest(String email) {
        this.email = email;
    }

}
