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

    public static InvoicingMessageEnum lookup(String v) {
        for (InvoicingMessageEnum e : values()) {
            if (e.value().equals(v)) {
                return e;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
