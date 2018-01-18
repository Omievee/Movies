package com.mobile.events;

public class UIRefreshEvent<E> {

    public enum EventName {
        USER_ZIP_CODE_CHANGED,
        USER_LOCATION_CHANGED
    }

    private EventName mEventName;
    private E mPayload;

    public UIRefreshEvent(EventName eventName, E payload) {
        this.mEventName = eventName;
        this.mPayload = payload;
    }

    public UIRefreshEvent(EventName eventName) {
        this.mEventName = eventName;
    }

    public E getPayload() {
        return mPayload;
    }

    public EventName getEventName() {
        return mEventName;
    }

}