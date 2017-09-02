package com.moviepass.requests;

/**
 * Created by anubis on 9/1/17.
 */

public class CancellationRequest {

    String requestDate;
    long  cancellationReason;
    String cancellationComment;

    public CancellationRequest(String requestDate, long cancellationReason, String cancellationComment) {
        this.requestDate = requestDate;
        this.cancellationReason = cancellationReason;
        this.cancellationComment = cancellationComment;
    }
}
