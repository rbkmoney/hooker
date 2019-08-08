package com.rbkmoney.hooker.model;

import lombok.*;

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
