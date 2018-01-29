package com.mobile.model;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Parcel
public class Movie implements ISearchable {

    protected int id;
    protected String tribuneId;
    protected String title;
    protected int runningTime;
    protected String releaseDate;
    protected String rating;
    protected String synopsis;
    protected boolean viewed;
    protected long createdAt;
    protected String imageUrl;
    protected String landscapeImageUrl;
    protected List<Review> reviews;
    protected String theaterName;

    public ArrayList<Movie> getReservations() {
        return reservations;
    }


    private ArrayList<Movie> reservations;

    public String getTheaterName() {
        return theaterName;
    }

    public String getLandscapeImageUrl() {
        return landscapeImageUrl;
    }


    public Movie() {
        reviews = new ArrayList<>();
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTribuneId() {
        return tribuneId;
    }

    public void setTribuneId(String tribuneId) {
        this.tribuneId = tribuneId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(int runningTime) {
        this.runningTime = runningTime;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getImageUrl() {
        if (this.imageUrl == null) {
            return "";
        }

        if (this.imageUrl.endsWith(".png") || this.imageUrl.endsWith(".jpg")) {
            return this.imageUrl;
        }
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }


}