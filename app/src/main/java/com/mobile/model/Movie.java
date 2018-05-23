package com.mobile.model;

import android.os.Parcelable;

import org.parceler.Parcel;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

@Parcel(value = Parcel.Serialization.BEAN, analyze = {Movie.class})
@RealmClass
public class Movie extends RealmObject implements ISearchable, Parcelable {

    protected int id;

    public Movie() {
    }

    protected String tribuneId;
    protected String title;
    protected int runningTime;
    protected String releaseDate;
    protected String rating;
    protected String synopsis;
    protected boolean viewed;
    protected long createdAt;

    protected Movie(android.os.Parcel in) {
        id = in.readInt();
        tribuneId = in.readString();
        title = in.readString();
        runningTime = in.readInt();
        releaseDate = in.readString();
        rating = in.readString();
        synopsis = in.readString();
        viewed = in.readByte() != 0;
        createdAt = in.readLong();
        userRating = in.readString();
        teaserVideoUrl = in.readString();
        type = in.readString();
        imageUrl = in.readString();
        landscapeImageUrl = in.readString();
        theaterName = in.readString();
    }

    @Override
    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(tribuneId);
        dest.writeString(title);
        dest.writeInt(runningTime);
        dest.writeString(releaseDate);
        dest.writeString(rating);
        dest.writeString(synopsis);
        dest.writeByte((byte) (viewed ? 1 : 0));
        dest.writeLong(createdAt);
        dest.writeString(userRating);
        dest.writeString(teaserVideoUrl);
        dest.writeString(type);
        dest.writeString(imageUrl);
        dest.writeString(landscapeImageUrl);
        dest.writeString(theaterName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(android.os.Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getUserRating() {
        return userRating;
    }

    public void setUserRating(String userRating) {
        this.userRating = userRating;
    }


    String userRating;

    public void setTeaserVideoUrl(String teaserVideoUrl) {
        this.teaserVideoUrl = teaserVideoUrl;
    }

    public String getTeaserVideoUrl() {
        return teaserVideoUrl;
    }

    String teaserVideoUrl;

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    String type;

    public void setLandscapeImageUrl(String landscapeImageUrl) {
        this.landscapeImageUrl = landscapeImageUrl;
    }

    public void setTheaterName(String theaterName) {
        this.theaterName = theaterName;
    }

//    public void setReservations(ArrayList<Movie> reservations) {
//        this.reservations = reservations;
//    }

    protected String imageUrl;
    protected String landscapeImageUrl;
    //    protected List<Review> reviews;
    protected String theaterName;

    //    public ArrayList<Movie> getReservations() {
//        return reservations;
//    }


//    private ArrayList<Movie> reservations;

    public String getTheaterName() {
        return theaterName;
    }

    public String getLandscapeImageUrl() {
        return landscapeImageUrl;
    }


//    public Movie() {
//        reviews = new ArrayList<>();
//    }

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

//    public List<Review> getReviews() {
//        return reviews;
//    }

//    public void setReviews(List<Review> reviews) {
//        this.reviews = reviews;
//    }


}