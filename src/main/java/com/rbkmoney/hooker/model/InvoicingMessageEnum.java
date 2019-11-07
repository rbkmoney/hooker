package com.rbkmoney.hooker.model;

import java.util.Arrays;

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

    public static InvoicingMessageEnum lookup(String v) {
        return Arrays.stream(values()).filter(value -> v.equals(value.value())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown invoicing message type: " + v));
    }
}
