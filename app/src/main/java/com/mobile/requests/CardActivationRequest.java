package com.mobile.requests;

/**
 * Created by anubis on 9/2/17.
 */

public class CardActivationRequest {

    String cardNumber;

    public CardActivationRequest(String last4digits) {
        this.cardNumber = last4digits;
    }
}
