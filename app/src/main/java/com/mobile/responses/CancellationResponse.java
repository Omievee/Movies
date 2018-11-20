package com.mobile.responses;

import io.reactivex.annotations.Nullable;

/**
 * Created by anubis on 9/1/17.
 */

public class CancellationResponse {

    String message;
    @Nullable
    String nextBillingDate;

    public String getNextBillingDate() {
        return nextBillingDate;
    }


    public String getMessage() {
        return message;
    }
}
