package com.rbkmoney.hooker.utils;

import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.InvoicingMessageEnum;
import com.rbkmoney.hooker.model.InvoicingMessageKey;

public class KeyUtils {

    public static InvoicingMessageKey key(InvoicingMessage message) {
        return InvoicingMessageKey.builder()
                .invoiceId(message.getInvoice().getId())
                .paymentId(message.getPayment() != null ? message.getPayment().getId() : null)
                .refundId(message.getRefund() != null ? message.getRefund().getId() : null)
                .type(InvoicingMessageEnum.lookup(message.getType()))
                .build();
    }
}

