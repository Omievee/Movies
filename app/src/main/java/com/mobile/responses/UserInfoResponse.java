package com.mobile.responses;

import com.mobile.model.UserInfo;

/**
 * Created by anubis on 8/1/17.
 */

public class UserInfoResponse {

    String authToken;
    UserInfo user;
    String billingAddressLine1;
    String billingAddressLine2;
    String billingCard;
    String nextBillingDate;
    String shippingAddressLine1;
    String shippingAddressLine2;
    String plan;
    String moviePassCardNumber;

    public String getMoviePassCardNumber() {
        return moviePassCardNumber;
    }


    public UserInfoResponse() {

    }

    public String getAuthToken() {
        return authToken;
    }

    public UserInfo getUser() {
        return user;
    }

    public String getBillingAddressLine1() {
        return billingAddressLine1;
    }

    public String getBillingAddressLine2() {
        return billingAddressLine2;
    }

    public String getBillingCard() {
        return billingCard;
    }

    public String getNextBillingDate() {
        return nextBillingDate;
    }

    public String getShippingAddressLine1() {
        return shippingAddressLine1;
    }

    public String getShippingAddressLine2() {
        return shippingAddressLine2;
    }

    public String getPlan() {
        return plan;
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getName() {
        return user.getFirstName() + " " + user.getLastName();
    }
}
