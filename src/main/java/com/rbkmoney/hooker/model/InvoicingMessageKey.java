package com.rbkmoney.hooker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
public class InvoicingMessageKey {
    private String invoiceId;
    private String paymentId;
    private String refundId;
    private InvoicingMessageEnum type;
}
