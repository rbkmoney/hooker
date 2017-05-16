package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by inalarsanukaev on 16.05.17.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonPropertyOrder({"code", "message"})
public class PaymentStatusError {
    private String code;
    private String message;

    public PaymentStatusError(PaymentStatusError other) {
        this.code = other.code;
        this.message = other.message;
    }
}
