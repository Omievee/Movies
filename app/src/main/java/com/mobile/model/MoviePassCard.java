package com.mobile.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by anubis on 8/1/17.
 */

@Parcel
public class MoviePassCard {

    @SerializedName("id")
    int id;
    @SerializedName("maskedNumber")
    String maskedNumber;
    @SerializedName("expirationDate")
    String expirationDate;
    @SerializedName("status")
    String status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMaskedNumber() {
        return maskedNumber;
    }

    public void setMaskedNumber(String maskedNumber) {
        this.maskedNumber = maskedNumber;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}