package com.mobile.responses;

/**
 * Created by anubis on 8/30/17.
 */

public class SignUpResponse {

    String status;
    String message;
    String subId;
    Errors errors;

    public String getSubId() { return subId; }

    public String getGlobal() { return errors.getGlobal(); }

    public class Errors {
        String global;

        public String getGlobal() { return global; }
    }
}
