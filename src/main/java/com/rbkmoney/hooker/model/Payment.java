package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.rbkmoney.swag_webhook_events.model.Payer;
import com.rbkmoney.swag_webhook_events.model.PaymentError;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by inalarsanukaev on 15.05.17.
 */
@NoArgsConstructor
@Getter
@Setter
public class Payment {
    private String id;
    private String createdAt;
    private String status;
    private PaymentError error;
    private long amount;
    private Long fee;
    private String currency;
    @JsonSerialize(using = MetadataSerializer.class)
    private Content metadata;
    private String paymentToolToken;
    private String paymentSession;
    private PaymentContactInfo contactInfo;
    private String ip;
    private String fingerprint;
    private Payer payer;
}
