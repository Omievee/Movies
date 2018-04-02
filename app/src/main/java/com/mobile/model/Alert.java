package com.mobile.model;

public class Alert {

    String id, title, body, urlTitle, url;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getUrlTitle() {
        return urlTitle;
    }

    public String getUrl() {
        return url;
    }

    public boolean isDismissable() {
        return dismissable;
    }

    boolean dismissable;

    public Alert() {
    }


}
