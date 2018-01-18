package com.mobile.responses;

/**
 * Created by anubis on 8/30/17.
 */

public class PersonalInfoResponse {

    String status;
    Errors errors;

    public Errors getErrors() {return this.errors;}


    public class Errors {
        String global;

        public String getGlobal() { return global; }

    }
}
