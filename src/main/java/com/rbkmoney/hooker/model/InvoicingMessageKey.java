package com.rbkmoney.hooker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class InvoicingMessageKey {
    private String invoiceId;
    private String paymentId;
    private String refundId;
    private InvoicingMessageEnum type;
}
