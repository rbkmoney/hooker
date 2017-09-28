package com.rbkmoney.hooker.model;

/**
 * Created by inalarsanukaev on 27.09.17.
 */
public class TaxMode {
    private String rate;

    public TaxMode(String rate) {
        this.rate = rate;
    }

    public TaxMode() {
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }
}
