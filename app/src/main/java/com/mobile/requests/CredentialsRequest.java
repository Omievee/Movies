package com.mobile.requests;

/**
 * Created by anubis on 8/30/17.
 */

public class CredentialsRequest {

    String step = "CREDENTIALS";
    PersonalInfo personalInfo;

    public CredentialsRequest(String email, String password, String confirmPassword) {
        this.personalInfo = new PersonalInfo(email, password, confirmPassword);
    }

    class PersonalInfo {
        String email;
        String password;
        String confirmPassword;

        public PersonalInfo(String email, String password, String confirm) {
            this.email = email;
            this.password = password;
            this.confirmPassword = confirm;
        }
    }
}
