package com.rbkmoney.hooker.model;

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
        for (CustomerMessageEnum e : values()) {
            if (e.value().equals(v)) {
                return e;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
