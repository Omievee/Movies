package com.mobile.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ivonne on 3/21/18.
 */

public class Plans implements Parcelable{

    private String id;
    private String name, price;
    private String planDescription, disclaimer;
    String isFeatured;
    private String paymentDisclaimer,confirmPlanDescription, confirmTotal;

    protected Plans(android.os.Parcel in) {
        id = in.readString();
        name = in.readString();
        price = in.readString();
        planDescription = in.readString();
        disclaimer = in.readString();
        isFeatured = in.readString();
        paymentDisclaimer = in.readString();
        confirmPlanDescription = in.readString();
        confirmTotal = in.readString();
    }

    public static final Creator<Plans> CREATOR = new Creator<Plans>() {
        @Override
        public Plans createFromParcel(android.os.Parcel in) {
            return new Plans(in);
        }

        @Override
        public Plans[] newArray(int size) {
            return new Plans[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(android.os.Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(price);
        parcel.writeString(planDescription);
        parcel.writeString(disclaimer);
        parcel.writeString(isFeatured);
        parcel.writeString(paymentDisclaimer);
        parcel.writeString(confirmPlanDescription);
        parcel.writeString(confirmTotal);
    }

    public Plans(String id, String name, String price, String planDescription, String disclaimer, String isFeatured, String paymentDisclaimer, String confirmPlanDescription, String confirmTotal) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.planDescription = planDescription;
        this.disclaimer = disclaimer;
        this.isFeatured = isFeatured;
        this.paymentDisclaimer = paymentDisclaimer;
        this.confirmPlanDescription = confirmPlanDescription;
        this.confirmTotal = confirmTotal;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPlanDescription() {
        return planDescription;
    }

    public void setPlanDescription(String planDescription) {
        this.planDescription = planDescription;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    public String getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(String isFeatured) {
        this.isFeatured = isFeatured;
    }

    public String getPaymentDisclaimer() {
        return paymentDisclaimer;
    }

    public void setPaymentDisclaimer(String paymentDisclaimer) {
        this.paymentDisclaimer = paymentDisclaimer;
    }

    public String getConfirmPlanDescription() {
        return confirmPlanDescription;
    }

    public void setConfirmPlanDescription(String confirmPlanDescription) {
        this.confirmPlanDescription = confirmPlanDescription;
    }

    public String getConfirmTotal() {
        return confirmTotal;
    }

    public void setConfirmTotal(String confirmTotal) {
        this.confirmTotal = confirmTotal;
    }
}
