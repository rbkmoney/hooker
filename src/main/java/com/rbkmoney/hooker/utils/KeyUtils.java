package com.rbkmoney.hooker.utils;

import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.InvoicingMessageKey;

public class KeyUtils {

    public static InvoicingMessageKey key(InvoicingMessage message) {
        return InvoicingMessageKey.builder()
                .invoiceId(message.getInvoiceId())
                .paymentId(message.getPaymentId())
                .refundId(message.getRefundId())
                .type(message.getType())
                .build();
    }
}

