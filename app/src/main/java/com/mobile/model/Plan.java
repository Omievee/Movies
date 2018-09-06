package com.mobile.model;


import kotlinx.android.parcel.Parcelize;

/**
 * Created by anubis on 7/12/17.
 */

@Parcelize
public class Plan {

    boolean amcUpgradeAble;
    String installationAmountInCents;
    String price;
    String shipping;
    String total;

    public boolean getAmcUpgradeAble() { return amcUpgradeAble; }

    public String getInstallationAmountInCents() { return installationAmountInCents; }

    public String getPrice() { return price; }
}
