package com.moviepass.activities;

/**
 * Created by o_vicarra on 11/9/17.
 */

import org.parceler.Parcel;

@Parcel

public class TicketType {


    String showtimeId;
    String id;
    int quantity;
    int price;

    public TicketType() {
    }

    public String getId() {
        return id;
    }

}
