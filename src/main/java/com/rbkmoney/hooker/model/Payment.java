package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by inalarsanukaev on 15.05.17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"id", "createdAt", "status", "error", "amount", "currency", "paymentToolToken", "paymentSession", "contactInfo", "ip", "fingerprint"})
public class Payment {
    private String id;
    private String createdAt;
    private String status;
    private PaymentStatusError error;
    private long amount;
    private String currency;
    private String paymentToolToken;
    private String paymentSession;
    private PaymentContactInfo contactInfo;
    private String ip;
    private String fingerprint;

    public Payment(Payment other) {
        this.id = other.id;
        this.createdAt = other.createdAt;
        this.status = other.status;
        if (other.error != null) {
            this.error = new PaymentStatusError(other.error);
        }
        this.amount = other.amount;
        this.currency = other.currency;
        this.paymentToolToken = other.paymentToolToken;
        this.paymentSession = other.paymentSession;
        if (other.contactInfo != null) {
            this.contactInfo = new PaymentContactInfo(other.contactInfo);
        }
        this.ip = other.ip;
        this.fingerprint = other.fingerprint;
    }
}
