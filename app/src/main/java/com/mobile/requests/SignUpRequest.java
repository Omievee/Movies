package com.mobile.requests;

/**
 * Created by anubis on 8/23/17.
 */

public class SignUpRequest {

    CreditCardInfo creditCardInfo;
    BillingAddress billingAddress;
    ShippingAddress shippingAddress;
    PersonalInfo personalInfo;
    Paypal paypal;
    String facebookToken;
    String birthday;
    String gender;
    int selectedPlanId;

    //CC & No FB
    public SignUpRequest(String number, String month, String year, String ccv,
                         String sStreet, String sStreet2, String sCity, String sState,
                         String sZip, String bStreet, String bStreet2, String bCity,
                         String bState, String bZip, String email, String firstName,
                         String lastName, String password, String birthday, String gender, String androidID) {

        this.creditCardInfo = new CreditCardInfo(number, month, year, ccv);
        this.billingAddress = new BillingAddress(bStreet, bStreet2, bCity, bState, bZip);
        this.shippingAddress = new ShippingAddress(sStreet, sStreet2, sCity, sState, sZip);
        this.personalInfo = new PersonalInfo(email, password, firstName, lastName, birthday, gender, androidID);

    }

    //CC & No FB && SelectedPlanId
    public SignUpRequest(String number, String month, String year, String ccv,
                         String sStreet, String sStreet2, String sCity, String sState,
                         String sZip, String bStreet, String bStreet2, String bCity,
                         String bState, String bZip, String email, String firstName,
                         String lastName, String password, String birthday, String gender, int selectedPlanId, String androidID) {

        this.creditCardInfo = new CreditCardInfo(number, month, year, ccv);
        this.billingAddress = new BillingAddress(bStreet, bStreet2, bCity, bState, bZip);
        this.shippingAddress = new ShippingAddress(sStreet, sStreet2, sCity, sState, sZip);
        this.personalInfo = new PersonalInfo(email, password, firstName, lastName, birthday, gender, androidID);
        this.selectedPlanId = selectedPlanId;

    }

    //CC & FB
//    public SignUpRequest(String number, String month, String year, String ccv,
//                         String sStreet, String sStreet2, String sCity, String sState,
//                         String sZip, String bStreet, String bStreet2, String bCity,
//                         String bState, String bZip, String email, String firstName,
//                         String lastName, String password, boolean amc3dMarkup, String facebookToken) {
//
//        this.amc3dMarkup = amc3dMarkup;
//        this.creditCardInfo = new CreditCardInfo(number, month, year, ccv);
//        this.billingAddress = new BillingAddress(bStreet, bStreet2, bCity, bState, bZip);
//        this.shippingAddress = new ShippingAddress(sStreet, sStreet2, sCity, sState, sZip);
//        this.personalInfo = new PersonalInfo(email, password, firstName, lastName);
//        this.facebookToken = facebookToken;
//    }

    //Paypal & facebook
    public SignUpRequest(String nonce,
                         String sStreet, String sStreet2, String sCity, String sState,
                         String sZip, String bStreet, String bStreet2, String bCity,
                         String bState, String bZip, String email, String firstName,
                         String lastName, String password, String facebookToken, String birthday, String gender, String androidID) {

        this.paypal = new Paypal(nonce);
        this.billingAddress = new BillingAddress(bStreet, bStreet2, bCity, bState, bZip);
        this.shippingAddress = new ShippingAddress(sStreet, sStreet2, sCity, sState, sZip);
        this.personalInfo = new PersonalInfo(email, password, firstName, lastName, birthday, gender, androidID);
        this.facebookToken = facebookToken;
    }

    //paypal & no facebook
    public SignUpRequest(String nonce,
                         String sStreet, String sStreet2, String sCity, String sState,
                         String sZip, String bStreet, String bStreet2, String bCity,
                         String bState, String bZip, String email, String firstName,
                         String lastName, String password, String birthday, String gender, String androidID) {

        this.paypal = new Paypal(nonce);
        this.billingAddress = new BillingAddress(bStreet, bStreet2, bCity, bState, bZip);
        this.shippingAddress = new ShippingAddress(sStreet, sStreet2, sCity, sState, sZip);
        this.personalInfo = new PersonalInfo(email, password, firstName, lastName, birthday, gender, androidID);
    }

    class PersonalInfo {
        String email;
        String password;
        String firstName;
        String lastName;
        String birthday;
        String gender;
        String androidID;

        PersonalInfo(String email, String password, String firstName, String lastName, String birthday, String gender, String androidID) {
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.password = password;
            this.birthday = birthday;
            this.gender = gender;
            this.androidID = androidID;
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

    class CreditCardInfo {
        String number;
        String expiration_month;
        String expiration_year;
        String ccv;

        CreditCardInfo(String number, String expiration_month, String expiration_year, String ccv) {
            this.number = number;
            this.expiration_month = expiration_month;
            this.expiration_year = expiration_year;
            this.ccv = ccv;
        }
    }

    class BillingAddress {
        String street;
        String street2;
        String city;
        String state;
        String zip;

        BillingAddress(String street, String street2, String city, String state, String zip) {
            this.street = street;
            this.street2 = street2;
            this.city = city;
            this.state = state;
            this.zip = zip;
        }
    }

    class Paypal {
        String nonce;

        Paypal(String nonce) {
            this.nonce = nonce;
        }
    }
}