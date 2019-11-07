package com.rbkmoney.hooker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum InvoicingMessageEnum {
    INVOICE("invoice"),
    PAYMENT("payment"),
    REFUND("refund");

    private String value;

    public static InvoicingMessageEnum lookup(String v) {
        return Arrays.stream(values()).filter(value -> v.equals(value.getValue())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown invoicing message type: " + v));
    }
}
