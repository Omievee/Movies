package com.moviepass.model;

import java.util.HashMap;

/**
 * Created by anubis on 6/10/17.
 */

public class Provider {

    public String providerName;
    public String theater;
    public String ticketType;
    public HashMap<String, PerformanceInfo> performanceInfo;

    public Provider() {
    }

    public PerformanceInfo getPerformanceInfo(String key) { return performanceInfo.get(key); }

}