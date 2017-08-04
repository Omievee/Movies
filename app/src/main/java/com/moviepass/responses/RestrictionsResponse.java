package com.moviepass.responses;

import com.moviepass.model.PopInfo;

/**
 * Created by anubis on 7/17/17.
 */

public class RestrictionsResponse {

    int countdown;
    String subscriptionStatus;
    boolean facebook;
    boolean has3d;
    boolean hasAllFormats;
    boolean proofOfPurchaseRequired;
    PopInfo popInfo;
    boolean hasActiveCard;

    public RestrictionsResponse() {
    }

    public int getCountdown() {
        return countdown;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public boolean getFacebookPresent() {return facebook; }

    public boolean get3dEnabled() { return has3d; }

    public boolean getAllFormatsEnabled() { return hasAllFormats; }

    public boolean getProofOfPurchaseRequired() { return proofOfPurchaseRequired; }

    public PopInfo getPopInfo() {return popInfo; }

    public boolean getHasActiveCard() {return hasActiveCard; }

}
