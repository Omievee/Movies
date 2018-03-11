package com.mobile.helpers;

import android.content.Context;

/**
 * Created by ivonneortega on 3/11/18.
 */

public class GoWatchItSingleton {

    private static GoWatchItSingleton instance;
    private String campaign;

    private GoWatchItSingleton() {
        campaign = "no_campaign";
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(String campaign) {
        if(campaign!=null)
            this.campaign = campaign;
    }

    public static GoWatchItSingleton getInstance() {

        synchronized (ContextSingleton.class) {
            if (instance == null) {
                instance = new GoWatchItSingleton();
            }
            return instance;
        }
    }

}
