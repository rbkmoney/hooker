package com.rbkmoney.hooker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum InvoiceStatusEnum {
    UNPAID("unpaid"),
    PAID("paid"),
    CANCELLED("cancelled"),
    FULFILLED("fulfilled");

    private String value;

    public static InvoiceStatusEnum lookup(String v) {
        return Arrays.stream(values()).filter(value -> v.equals(value.getValue())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown invoice status: " + v));
    }
}
