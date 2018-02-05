package com.mobile.model;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by anubis on 6/10/17.
 */

@Parcel
public class Screening implements Comparator<Theater>{

    private List<Screening> screening;

    LinkedHashMap<String, Boolean> availabilities = new LinkedHashMap<String, Boolean>();
    String availability;
    String date;
    List<String> endTimes = new ArrayList<String>();
    String format;
    int id;
    String imageUrl;
    String landscapeImageUrl;
    String kind;
    int moviepassId;
    String programType;
    Provider provider;
    String qualifiers;
    boolean qualifiersApproved;
    String releaseDate;
    int runningTime;
    String screen;
    List<String> startTimes = new ArrayList<String>();
    String status;
    String theaterAddress;
    String theaterName;
    String title;
    String rating;
    String synopsis;




    public String getSynopsis() {
        return synopsis;
    }

    public String getRating() {
        return rating;
    }


    int tribuneTheaterId;


    public Screening() {
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getRunningTime() {
        return runningTime;
    }

    public String getTitle() {
        return title;
    }

    public int getMoviepassId() {
        return moviepassId;
    }

    public String getLandscapeImageUrl() {
        return landscapeImageUrl;
    }

    public void setMoviepassId(int moviepassId) {
        this.moviepassId = moviepassId;
    }

    public int getTribuneTheaterId() {
        return tribuneTheaterId;
    }

    public void setTribuneTheaterId(int tribuneTheaterId) {
        this.tribuneTheaterId = tribuneTheaterId;
    }

    public String getTheaterName() {
        return theaterName;
    }

    public void setTheaterName(String theaterName) {
        this.theaterName = theaterName;
    }

    public List<String> getStartTimes() {
        return startTimes;
    }

    public void setStartTimes(List<String> startTimes) {
        this.startTimes = startTimes;
    }

    public List<String> getEndTimes() {
        return endTimes;
    }

    public void setEndTimes(List<String> endTimes) {
        this.endTimes = endTimes;
    }

    public LinkedHashMap<String, Boolean> getAvailabilities() {
        return availabilities;
    }

    public void setAvailabilities(LinkedHashMap<String, Boolean> availabilities) {
        this.availabilities = availabilities;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getTheaterAddress() {
        return theaterAddress;
    }

    public void setTheaterAddress(String theaterAddress) {
        this.theaterAddress = theaterAddress;
    }

    public String getQualifiers() {
        return qualifiers;
    }

    public void setQualifiers(String qualifiers) {
        this.qualifiers = qualifiers;
    }

    public boolean getQualifiersApproved() {
        return qualifiersApproved;
    }

    public void setQualifiersApproved(boolean qualifiersApproved) {
        this.qualifiersApproved = qualifiersApproved;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getProgramType() {
        return programType;
    }

    public void setProgramType(String programType) {
        this.programType = programType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

/*    public String getTicketType() { return provider.getTicketType(); }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public boolean ticketTypeExists() {
        return ticketType != null;
    }

    public boolean ticketTypeIsStandard() {
        return provider.ticketType.matches("STANDARD");
    }

    public boolean ticketTypeIsETicket() {
        return provider.ticketType.matches("E_TICKET");
    }

    public boolean ticketTypeIsSelectSeating() {
        return provider.ticketType.matches("SELECT_SEATING");
    }

    */

    public boolean formatExists() {
        return format != null;
    }

    public boolean is3D() {
        return (format.toLowerCase().contains("r3d") ||
                format.toLowerCase().contains("real3d") ||
                format.toLowerCase().contains("3d") ||
                qualifiers.toLowerCase().contains("real3d"));
    }

    public boolean is2D() {
        return format.toLowerCase().matches("2d");
    }

    public boolean isImax() {
        return format.toLowerCase().contains("imax") || qualifiers.toLowerCase().matches("imax");
    }

    public boolean isEtx() {
        return format.toLowerCase().matches("etx") || qualifiers.toLowerCase().contains("etx");
    }

    public boolean isRpx() {
        return format.toLowerCase().contains("rpx") || qualifiers.toLowerCase().contains("rpx");
    }

    public boolean isLargeFormat() {
        return
                format.toLowerCase().matches("etx") || qualifiers.toLowerCase().contains("etx") ||
                        format.toLowerCase().contains("imax") || qualifiers.toLowerCase().matches("imax") ||
                        format.toLowerCase().contains("rpx") || qualifiers.toLowerCase().contains("rpx");
    }

    public boolean isTheatreEvent() {
        return programType.toLowerCase().matches("theatre event");
    }

    public boolean isApproved() {

        if (status == null || status.toLowerCase().matches("not_approved"))
            return false;

        if (!qualifiersApproved)
            return false;

        return true;
    }

    public String getProviderName() {
        return provider.getProviderName();
    }


    public Provider getProvider() {
        return provider;
    }


    @Override
    public int compare(Theater theater, Theater t1) {
        return Double.compare(theater.getDistance(), t1.getDistance());
    }
}
