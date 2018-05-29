package com.mobile.model;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by anubis on 6/10/17.
 */

@Parcel
public class Screening {

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

    public void setTitle(String title) {
        this.title = title;
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


    public void setMoviepassId(int moviepassId) {
        this.moviepassId = moviepassId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Screening screening = (Screening) o;

        if (id != screening.id) return false;
        if (moviepassId != screening.moviepassId) return false;
        if (approved != screening.approved) return false;
        if (qualifiersApproved != screening.qualifiersApproved) return false;
        if (runningTime != screening.runningTime) return false;
        if (popRequired != screening.popRequired) return false;
        if (tribuneTheaterId != screening.tribuneTheaterId) return false;
        if (availabilities != null ? !availabilities.equals(screening.availabilities) : screening.availabilities != null)
            return false;
        if (availability != null ? !availability.equals(screening.availability) : screening.availability != null)
            return false;
        if (date != null ? !date.equals(screening.date) : screening.date != null) return false;
        if (endTimes != null ? !endTimes.equals(screening.endTimes) : screening.endTimes != null)
            return false;
        if (format != null ? !format.equals(screening.format) : screening.format != null)
            return false;
        if (imageUrl != null ? !imageUrl.equals(screening.imageUrl) : screening.imageUrl != null)
            return false;
        if (landscapeImageUrl != null ? !landscapeImageUrl.equals(screening.landscapeImageUrl) : screening.landscapeImageUrl != null)
            return false;
        if (kind != null ? !kind.equals(screening.kind) : screening.kind != null) return false;
        if (programType != null ? !programType.equals(screening.programType) : screening.programType != null)
            return false;
        if (provider != null ? !provider.equals(screening.provider) : screening.provider != null)
            return false;
        if (disabledExplanation != null ? !disabledExplanation.equals(screening.disabledExplanation) : screening.disabledExplanation != null)
            return false;
        if (qualifiers != null ? !qualifiers.equals(screening.qualifiers) : screening.qualifiers != null)
            return false;
        if (releaseDate != null ? !releaseDate.equals(screening.releaseDate) : screening.releaseDate != null)
            return false;
        if (screen != null ? !screen.equals(screening.screen) : screening.screen != null)
            return false;
        if (startTimes != null ? !startTimes.equals(screening.startTimes) : screening.startTimes != null)
            return false;
        if (status != null ? !status.equals(screening.status) : screening.status != null)
            return false;
        if (theaterAddress != null ? !theaterAddress.equals(screening.theaterAddress) : screening.theaterAddress != null)
            return false;
        if (theaterName != null ? !theaterName.equals(screening.theaterName) : screening.theaterName != null)
            return false;
        if (title != null ? !title.equals(screening.title) : screening.title != null) return false;
        if (rating != null ? !rating.equals(screening.rating) : screening.rating != null)
            return false;
        return synopsis != null ? synopsis.equals(screening.synopsis) : screening.synopsis == null;
    }

    @Override
    public int hashCode() {
        int result = availabilities != null ? availabilities.hashCode() : 0;
        result = 31 * result + (availability != null ? availability.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (endTimes != null ? endTimes.hashCode() : 0);
        result = 31 * result + (format != null ? format.hashCode() : 0);
        result = 31 * result + id;
        result = 31 * result + (imageUrl != null ? imageUrl.hashCode() : 0);
        result = 31 * result + (landscapeImageUrl != null ? landscapeImageUrl.hashCode() : 0);
        result = 31 * result + (kind != null ? kind.hashCode() : 0);
        result = 31 * result + moviepassId;
        result = 31 * result + (programType != null ? programType.hashCode() : 0);
        result = 31 * result + (provider != null ? provider.hashCode() : 0);
        result = 31 * result + (approved ? 1 : 0);
        result = 31 * result + (disabledExplanation != null ? disabledExplanation.hashCode() : 0);
        result = 31 * result + (qualifiers != null ? qualifiers.hashCode() : 0);
        result = 31 * result + (qualifiersApproved ? 1 : 0);
        result = 31 * result + (releaseDate != null ? releaseDate.hashCode() : 0);
        result = 31 * result + runningTime;
        result = 31 * result + (screen != null ? screen.hashCode() : 0);
        result = 31 * result + (startTimes != null ? startTimes.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (theaterAddress != null ? theaterAddress.hashCode() : 0);
        result = 31 * result + (theaterName != null ? theaterName.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (rating != null ? rating.hashCode() : 0);
        result = 31 * result + (synopsis != null ? synopsis.hashCode() : 0);
        result = 31 * result + (popRequired ? 1 : 0);
        result = 31 * result + tribuneTheaterId;
        return result;
    }
}
