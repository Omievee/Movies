package com.mobile.model;

/**
 * Created by anubis on 6/27/17.
 */

import org.parceler.Parcel;

import java.util.Random;

@Parcel
public class SeatInfo implements Comparable<SeatInfo> {

    public enum SeatType {
        SeatTypeUnknown,
        SeatTypeCanReserve,
        SeatTypeCanReserveLeft,
        SeatTypeCanReserveRight,
        SeatTypeWheelchair,
        SeatTypeCompanion,
        SeatTypeNotASeat,
        SeatTypeSofaLeft,
        SeatTypeSofaMiddle,
        SeatTypeSofaRight


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
        } else if (type.toLowerCase().matches("sofaleft")) {
            seatType = SeatType.SeatTypeSofaLeft;
        } else if (type.toLowerCase().matches("sofaright")) {
            seatType = SeatType.SeatTypeSofaRight;
        } else if (type.toLowerCase().matches("sofamiddle")) {
            seatType = SeatType.SeatTypeSofaMiddle;
        }
        return seatType;
    }

    public boolean isWheelChairOrCompanion() {
        SeatType type = getSeatType();
        return type == SeatType.SeatTypeWheelchair || type == SeatType.SeatTypeCompanion;
    }

    @Override
    public int compareTo(SeatInfo another) {
        SeatInfo ss = (SeatInfo) another;

        if (row == ss.row)
            return this.column - ss.column;
        else
            return this.row - ss.row;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SeatInfo seatInfo = (SeatInfo) o;

        if (row != seatInfo.row) return false;
        if (column != seatInfo.column) return false;
        if (seatName != null ? !seatName.equals(seatInfo.seatName) : seatInfo.seatName != null)
            return false;
        return type != null ? type.equals(seatInfo.type) : seatInfo.type == null;
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + column;
        result = 31 * result + (seatName != null ? seatName.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SeatInfo{" +
                "available=" + available +
                ", row=" + row +
                ", column=" + column +
                ", seatName='" + seatName + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}