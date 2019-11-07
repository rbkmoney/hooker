package com.rbkmoney.hooker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum PaymentStatusEnum {
    PENDING("pending"),
    PROCESSED("processed"),
    CAPTURED("captured"),
    CANCELLED("cancelled"),
    REFUNDED("refunded"),
    FAILED("failed");

    private String value;

    public static PaymentStatusEnum lookup(String v) {
        return Arrays.stream(values()).filter(value -> v.equals(value.getValue())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown payment status: " + v));
    }
}
