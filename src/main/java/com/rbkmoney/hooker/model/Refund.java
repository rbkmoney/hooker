package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rbkmoney.swag_webhook_events.model.PaymentError;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
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

}
