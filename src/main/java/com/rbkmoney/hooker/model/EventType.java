package com.rbkmoney.hooker.model;

/**
 * Created by inal on 30.11.2016.
 */
public enum EventType {
    INVOICE_CREATED               ("source_event.processing_event.payload.invoice_event.invoice_created.invoice"),
    INVOICE_STATUS_CHANGED        ("source_event.processing_event.payload.invoice_event.invoice_status_changed.status"),
    INVOICE_PAYMENT_STARTED       ("source_event.processing_event.payload.invoice_event.invoice_payment_event.invoice_payment_started.payment"),
    INVOICE_PAYMENT_STATUS_CHANGED("source_event.processing_event.payload.invoice_event.invoice_payment_event.invoice_payment_status_changed.status");

    private String thriftFilterPathCoditionRule;

    EventType(String thriftFilterPathCoditionRule) {
        this.thriftFilterPathCoditionRule = thriftFilterPathCoditionRule;
    }

    public String getThriftFilterPathCoditionRule() {
        return thriftFilterPathCoditionRule;
    }
}
