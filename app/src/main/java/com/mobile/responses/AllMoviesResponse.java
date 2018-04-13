package com.mobile.responses;

import com.mobile.model.Movie;

import java.util.List;

public class AllMoviesResponse {
    String id;
    String title;
    String titleNormalized;
    String tomatoRating;
    String runningTime;
    String releaseDate;
    String rating;
    String synopsis;
    String viewed;
    String createdAt;
    String imageUrl;
    //ADD REVIEWS
    String landscapeImageUrl;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleNormalized() {
        return titleNormalized;
    }

    public String getTomatoRating() {
        return tomatoRating;
    }

    public String getRunningTime() {
        return runningTime;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getRating() {
        return rating;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public String getViewed() {
        return viewed;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLandscapeImageUrl() {
        return landscapeImageUrl;
    }
}
