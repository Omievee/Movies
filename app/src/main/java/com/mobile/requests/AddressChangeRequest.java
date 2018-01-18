package com.mobile.requests;

/**
 * Created by anubis on 8/1/17.
 */

public class AddressChangeRequest {

    String street;
    String street2;
    String city;
    String state;
    String zip;
    String section;

    public AddressChangeRequest(String street, String street2, String city, String state, String zip, String section) {
        this.street = street;
        this.street2 = street2;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.section = section;
    }
}
