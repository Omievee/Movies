package com.moviepass.requests;

/**
 * Created by anubis on 8/30/17.
 */

public class PersonalInfoRequest {

    String step = "PERSONAL_INFO";
    PersonalInfo personalInfo;
    ShippingAddress shippingAddress;

    public PersonalInfoRequest(String email, String password, String confirmPassword,
                               String firstName, String lastName, String street, String street2,
                               String city, String state, String zip) {

        this.personalInfo = new PersonalInfo(email, password, confirmPassword, firstName, lastName);
        this.shippingAddress = new ShippingAddress(street, street2, city, state, zip);
    }

    class PersonalInfo {
        String email;
        String password;
        String confirmPassword;
        String firstName;
        String lastName;

        PersonalInfo(String email, String password, String confirmPassword, String firstName, String lastName) {
            this.email = email;
            this.password = password;
            this.confirmPassword = confirmPassword;
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }

    class ShippingAddress {
        String street;
        String street2;
        String city;
        String state;
        String zip;

        ShippingAddress(String street, String street2, String city, String state, String zip) {
            this.street = street;
            this.street2 = street2;
            this.city = city;
            this.state = state;
            this.zip = zip;
        }
    }
}
