package com.moviepass.model;

/**
 * Created by anubis on 6/10/17.
 */

public class PerformanceInfo {

    public String dateTime;
    public String externalMovieId;
    public String format;
    public int normalizedMovieId;
    public String performanceNumber;
    public String price;
    public String sku;
    public int tribuneTheaterId;

    public PerformanceInfo() {
    }

    public String getDateTime() { return dateTime; }

    public String getExternalMovieId() { return externalMovieId; }

    public String getFormat() { return format; }

    public int getNormalizedMovieId() { return normalizedMovieId; }

    public int getTribuneTheaterId() { return tribuneTheaterId; }
}
