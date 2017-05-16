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
public class Payment implements Serializable {
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
}
