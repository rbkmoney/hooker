package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by inalarsanukaev on 15.05.17.
 */
@JsonPropertyOrder({"id", "createdAt", "status", "error", "amount", "currency", "paymentToolToken", "paymentSession", "contactInfo", "ip", "fingerprint"})
public class Payment {
    private String id;
    private String createdAt;
    private String status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PaymentStatusError error;
    private long amount;
    private String currency;
    private String paymentToolToken;
    private String paymentSession;
    private PaymentContactInfo contactInfo;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ip;
    @JsonInclude(JsonInclude.Include.NON_NULL)
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

    public Payment() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PaymentStatusError getError() {
        return error;
    }

    public void setError(PaymentStatusError error) {
        this.error = error;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentToolToken() {
        return paymentToolToken;
    }

    public void setPaymentToolToken(String paymentToolToken) {
        this.paymentToolToken = paymentToolToken;
    }

    public String getPaymentSession() {
        return paymentSession;
    }

    public void setPaymentSession(String paymentSession) {
        this.paymentSession = paymentSession;
    }

    public PaymentContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(PaymentContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }
}
