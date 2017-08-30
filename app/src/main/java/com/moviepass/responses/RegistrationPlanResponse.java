package com.moviepass.responses;

/**
 * Created by anubis on 8/30/17.
 */

public class RegistrationPlanResponse {

    String installationAmountInCents;
    boolean amcUpgradeAble;
    String total;
    String shipping;
    String price;
    int legacyId;
    String name;
    int signUpFee;
    int installmentAmount;
    String numberOfInstallments;

    public boolean getAmcUpgradeAble() { return amcUpgradeAble; }

    public String getInstallationAmountInCents() { return installationAmountInCents; }

    public String getPrice() { return price; }
}
