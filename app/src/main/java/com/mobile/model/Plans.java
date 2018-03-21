package com.mobile.model;

/**
 * Created by ivonne on 3/21/18.
 */

public class Plans {

    private String id;
    private String name, price;
    private String planDescription, disclaimer;
    private String paymentDisclaimer,confirmPlanDescription, confirmTotal;

    public Plans(String id, String name, String price, String planDescription, String disclaimer, String paymentDisclaimer, String confirmPlanDescription, String confirmTotal) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.planDescription = planDescription;
        this.disclaimer = disclaimer;
        this.paymentDisclaimer = paymentDisclaimer;
        this.confirmPlanDescription = confirmPlanDescription;
        this.confirmTotal = confirmTotal;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPlanDescription() {
        return planDescription;
    }

    public void setPlanDescription(String planDescription) {
        this.planDescription = planDescription;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    public String getPaymentDisclaimer() {
        return paymentDisclaimer;
    }

    public void setPaymentDisclaimer(String paymentDisclaimer) {
        this.paymentDisclaimer = paymentDisclaimer;
    }

    public String getConfirmPlanDescription() {
        return confirmPlanDescription;
    }

    public void setConfirmPlanDescription(String confirmPlanDescription) {
        this.confirmPlanDescription = confirmPlanDescription;
    }

    public String getConfirmTotal() {
        return confirmTotal;
    }

    public void setConfirmTotal(String confirmTotal) {
        this.confirmTotal = confirmTotal;
    }
}
