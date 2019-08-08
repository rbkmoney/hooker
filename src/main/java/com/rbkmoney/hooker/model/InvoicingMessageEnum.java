package com.rbkmoney.hooker.model;

public enum InvoicingMessageEnum {
    INVOICE("invoice"),
    PAYMENT("payment"),
    REFUND("refund");

    private String value;

    InvoicingMessageEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
