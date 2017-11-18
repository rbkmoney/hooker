package com.rbkmoney.hooker.model;

/**
 * Created by inalarsanukaev on 14.11.17.
 */
public class CustomerQueue extends Queue{
    private long customerId;

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }
}
