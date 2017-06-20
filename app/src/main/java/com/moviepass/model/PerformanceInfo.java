package com.moviepass.model;

/**
 * Created by anubis on 6/10/17.
 */

public class PerformanceInfo {

    String externalMovieId;
    String format;
    int tribuneTheaterId;
    int screeningId;
    String dateTime;
    int performanceNumber;
    String sku;
    Double price;
    String auditorium;
    String performanceId;
    String sessionId;
    int normalizedMovieId;

    public PerformanceInfo() {
    }

    public String getExternalMovieId() { return externalMovieId; }

    public String getDateTime() { return dateTime; }

    public int getNormalizedMovieId() { return normalizedMovieId; }

    public int getScreeningId() { return screeningId; }

    public String getFormat() { return format; }

    public int getTribuneTheaterId() { return tribuneTheaterId; }

    public int getPerformanceNumber() { return  performanceNumber; }

    public String getSku() { return sku; }

    public Double getPrice() { return price; }

    public String getAuditorium() { return auditorium; }

    public String getPerformanceId() { return performanceId; }

    public String getSessionId() { return sessionId; }
}
