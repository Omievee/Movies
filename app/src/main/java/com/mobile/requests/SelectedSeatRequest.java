package com.mobile.requests;

/**
 * Created by anubis on 9/3/17.
 */

public class SelectedSeatRequest {

    int row;
    int column;

    public SelectedSeatRequest(int row, int column) {
        this.row = row;
        this.column = column;
    }
}
