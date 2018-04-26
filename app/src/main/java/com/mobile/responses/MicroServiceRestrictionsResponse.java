package com.mobile.responses;

import com.mobile.model.Alert;
import com.mobile.model.LogoutInfo;
import com.mobile.model.PopInfo;

public class MicroServiceRestrictionsResponse {


    public MicroServiceRestrictionsResponse() {
    }


    int countdown;
    String subscriptionStatus;
    boolean facebook;
    boolean has3d;
    boolean hasAllFormats;
    boolean proofOfPurchaseRequired;
    PopInfo popInfo;
    boolean hasActiveCard;
    Alert alert;
    LogoutInfo logoutInfo;

    public LogoutInfo getLogoutInfo() {
        return logoutInfo;
    }

    public boolean isSubscriptionActivationRequired() {
        return subscriptionActivationRequired;
    }

    boolean subscriptionActivationRequired;


    public int getCountdown() {
        return countdown;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public boolean getFacebookPresent() {
        return facebook;
    }

    public boolean get3dEnabled() {
        return has3d;
    }

    public boolean getAllFormatsEnabled() {
        return hasAllFormats;
    }

    public boolean getProofOfPurchaseRequired() {
        return proofOfPurchaseRequired;
    }

    public PopInfo getPopInfo() {
        return popInfo;
    }

    public boolean getHasActiveCard() {
        return hasActiveCard;
    }

    public Alert getAlert() {
        return alert;
    }
}
