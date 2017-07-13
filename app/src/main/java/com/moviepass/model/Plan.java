package com.moviepass.model;

import org.parceler.Parcel;

/**
 * Created by anubis on 7/12/17.
 */

@Parcel
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
