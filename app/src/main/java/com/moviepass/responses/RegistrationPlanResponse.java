package com.moviepass.responses;

/**
 * Created by anubis on 7/13/17.
 */

public class RegistrationPlanResponse {

    boolean amcUpgradeAble;
    String installationAmountInCents;
    String price;
    String shipping;
    String total;



    public boolean getAmcUpgradeAble() { return amcUpgradeAble; }

    public String getInstallationAmountInCents() { return installationAmountInCents; }

    public String getPrice() { return price; }

    public String getTotal() { return total; }
}
