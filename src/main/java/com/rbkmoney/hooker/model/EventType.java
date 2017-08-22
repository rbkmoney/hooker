package com.rbkmoney.hooker.model;

/**
 * Created by inal on 30.11.2016.
 */
public enum EventType {
    INVOICE_CREATED("invoice_created"),
    INVOICE_STATUS_CHANGED("invoice_status_changed"),
    INVOICE_PAYMENT_STARTED("invoice_payment_change.payload.invoice_payment_started"),
    INVOICE_PAYMENT_STATUS_CHANGED("invoice_payment_change.payload.invoice_payment_status_changed"),
    INVOICE_PAYMENT_REFUND_PROCESSED("invoice_payment_change.payload.invoice_payment_refund_change.payload.invoice_payment_refund_created");

    private String thriftFilterPathCoditionRule;

    EventType(String thriftFilterPathCoditionRule) {
        this.thriftFilterPathCoditionRule = thriftFilterPathCoditionRule;
    }

    public String getThriftFilterPathCoditionRule() {
        return thriftFilterPathCoditionRule;
    }
}
