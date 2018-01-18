package com.mobile.requests;

import com.google.gson.annotations.SerializedName;

public class CreditCardChangeRequest {

    String section = "creditCardInfo";
    String number;
    String ccv;

    @SerializedName("creditCardInfo.expirationDate")
    String expirationDate;


    public CreditCardChangeRequest(String number, String expDate, String ccv) {
        this.number = number;
        this.ccv = ccv;
        this.expirationDate = expDate;
    }

}