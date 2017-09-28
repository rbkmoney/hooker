package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by inalarsanukaev on 16.05.17.
 */
@JsonPropertyOrder({"email", "phoneNumber"})
public class PaymentContactInfo {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String email;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String phoneNumber;

    public PaymentContactInfo(PaymentContactInfo other) {
        this.email = other.email;
        this.phoneNumber = other.phoneNumber;
    }

    public PaymentContactInfo(String email, String phoneNumber) {
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public PaymentContactInfo() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
