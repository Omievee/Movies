package com.moviepass.model;

import org.parceler.Parcel;

import java.util.HashMap;

/**
 * Created by anubis on 6/10/17.
 */

@Parcel
public class Provider {

    public String providerName;
    public int theater;
    public String ticketType;
    public HashMap<String, PerformanceInfo> performanceInfo;

    public Provider() {
    }

    public PerformanceInfo getPerformanceInfo(String key) { return performanceInfo.get(key); }

    public int getTheater() { return theater; }


}
