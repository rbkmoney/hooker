package com.rbkmoney.hooker.model;

import java.util.Arrays;

public enum CustomerMessageEnum {
    CUSTOMER("customer"),
    BINDING("binding");

    private String value;

    CustomerMessageEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static CustomerMessageEnum lookup(String v) {
        return Arrays.stream(values()).filter(value -> v.equals(value.value())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown customer message type: " + v));
    }
}
