package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rbkmoney.swag_webhook_events.PaymentError;

public class Refund {
    private String id;
    private String createdAt;
    private String status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PaymentError error;
    private Long amount;
    private String currency;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String reason;

    public Refund(Refund other) {
        this.id = other.id;
        this.createdAt = other.createdAt;
        this.status = other.status;
        if (other.error != null) {
            this.error = new PaymentError();
            this.error.setCode(other.error.getCode());
            this.error.setMessage(other.error.getMessage());
            this.error.setSubError(other.error.getSubError());
        }
        this.amount = other.amount;
        this.currency = other.currency;
        this.reason = other.reason;
    }

    public Refund() {

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

    public PaymentError getError() {
        return error;
    }

    public void setError(PaymentError error) {
        this.error = error;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
