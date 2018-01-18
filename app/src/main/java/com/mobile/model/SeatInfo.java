package com.mobile.model;

/**
 * Created by anubis on 6/27/17.
 */

import org.parceler.Parcel;

@Parcel
public class SeatInfo implements Comparable {

    public enum SeatType {
        SeatTypeUnknown,
        SeatTypeCanReserve,
        SeatTypeCanReserveLeft,
        SeatTypeCanReserveRight,
        SeatTypeWheelchair,
        SeatTypeCompanion,
        SeatTypeNotASeat
    }

    boolean available;
    int row;
    int column;
    String seatName;
    String type;

    public SeatInfo() {
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getSeatName() {
        return seatName;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SeatType getSeatType() {
        SeatType seatType = SeatType.SeatTypeUnknown;

        if (type.toLowerCase().matches("canreserve")) {
            seatType = SeatType.SeatTypeCanReserve;
        }
        if (type.toLowerCase().matches("canreserveleft")) {
            seatType = SeatType.SeatTypeCanReserveLeft;
        }
        if (type.toLowerCase().matches("canreserveright")) {
            seatType = SeatType.SeatTypeCanReserveRight;
        } else if (type.toLowerCase().matches("wheelchair")) {
            seatType = SeatType.SeatTypeWheelchair;
        } else if (type.toLowerCase().matches("companion")) {
            seatType = SeatType.SeatTypeCompanion;
        } else if (type.toLowerCase().matches("notaseat")) {
            seatType = SeatType.SeatTypeNotASeat;
        }

        return seatType;
    }

    @Override
    public int compareTo(Object another) {
        SeatInfo ss = (SeatInfo) another;

        if (row == ss.row)
            return this.column - ss.column;
        else
            return this.row - ss.row;
    }
}