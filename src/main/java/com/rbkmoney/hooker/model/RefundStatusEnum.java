package com.rbkmoney.hooker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum RefundStatusEnum {
    PENDING("pending"),
    SUCCEEDED("succeeded"),
    FAILED("failed");

    private String value;

    public static RefundStatusEnum lookup(String v) {
        return Arrays.stream(values()).filter(value -> v.equals(value.getValue())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown refund status: " + v));
    }
}
