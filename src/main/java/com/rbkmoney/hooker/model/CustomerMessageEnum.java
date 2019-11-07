package com.rbkmoney.hooker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CustomerMessageEnum {
    CUSTOMER("customer"),
    BINDING("binding");

    private String value;

    public static CustomerMessageEnum lookup(String v) {
        return Arrays.stream(values()).filter(value -> v.equals(value.getValue())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown customer message type: " + v));
    }
}
