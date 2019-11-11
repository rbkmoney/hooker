package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by inalarsanukaev on 16.05.17.
 */
@JsonPropertyOrder({"email", "phoneNumber"})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaymentContactInfo {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String email;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String phoneNumber;

    public PaymentContactInfo(PaymentContactInfo other) {
        this.email = other.email;
        this.phoneNumber = other.phoneNumber;
    }
}