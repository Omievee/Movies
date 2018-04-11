package com.mobile.model;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import io.realm.RealmObject;

/**
 * Created by anubis on 6/10/17.
 */

@Parcel
public class Screening {

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


    boolean approved;
    String disabledExplanation;
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


    boolean popRequired;

    public String getDisabledExplanation() {
        return disabledExplanation;
    }

    public boolean isApproved() {
        return approved;
    }

    public boolean isPopRequired() {
        return popRequired;
    }

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


    public String getKind() {
        return kind;
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


    public Provider getProvider() {
        return provider;
    }


}
