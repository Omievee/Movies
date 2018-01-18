package com.mobile.model;

import org.parceler.Parcel;

/**
 * Created by anubis on 6/20/17.
 */

@Parcel
public class SelectedSeat {

    int row;
    int column;
    String seatName;

    public SelectedSeat() {
    }

    public SelectedSeat(int row, int column, String seatName) {
        this.row = row;
        this.column = column;
        this.seatName = seatName;
    }

    public int getSelectedSeatRow() { return row; }

    public int getSelectedSeatColumn() { return column; }

    public String getSeatName() { return seatName; }
}