package com.mobile.model;

import org.parceler.Parcel;

import java.util.List;

/**
 * Created by anubis on 6/27/17.
 */

@Parcel
public class SeatingsInfo {

    public int rows;
    public int columns;
    public List<SeatInfo> seats;
}
