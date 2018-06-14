package com.mobile.responses;

public class ReferAFriendResponse {

    String emailMessage;
    String emailSubject;
    String referralTitle;

    public String getReferralTitle() {
        return referralTitle;
    }

    public String getReferralMessage() {
        return referralMessage;
    }

    String referralMessage;



    public ReferAFriendResponse() {
    }

    public String getEmailMessage() {
        return emailMessage;
    }

    public String getEmailSubject() {
        return emailSubject;
    }
}
