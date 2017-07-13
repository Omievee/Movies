package com.moviepass.model;

/**
 * Created by anubis on 7/12/17.
 */

public class Plan {

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
