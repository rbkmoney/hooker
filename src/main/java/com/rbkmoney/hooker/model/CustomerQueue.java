package com.rbkmoney.hooker.model;

/**
 * Created by inalarsanukaev on 14.11.17.
 */
public class CustomerQueue extends Queue {
    private String customerId;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
