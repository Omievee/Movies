package com.mobile.requests;

/**
 * Created by anubis on 9/2/17.
 */

public class CardActivationRequest {

    String last4digits;

    public CardActivationRequest(String last4digits) {
        this.last4digits = last4digits;
    }
}
