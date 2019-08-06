package com.rbkmoney.hooker.model;

/**
 * Created by inal on 30.11.2016.
 */
public enum EventType {
    INVOICE_CREATED("invoice_created"),
    INVOICE_STATUS_CHANGED("invoice_status_changed"),
    INVOICE_PAYMENT_STARTED("invoice_payment_change.payload.invoice_payment_started"),
    INVOICE_PAYMENT_STATUS_CHANGED("invoice_payment_change.payload.invoice_payment_status_changed"),
    INVOICE_PAYMENT_REFUND_STARTED("invoice_payment_change.payload.invoice_payment_refund_change.payload.invoice_payment_refund_created"),
    INVOICE_PAYMENT_REFUND_STATUS_CHANGED("invoice_payment_change.payload.invoice_payment_refund_change.payload.invoice_payment_refund_status_changed"),
    INVOICE_PAYMENT_CASH_FLOW_CHANGED("invoice_payment_change.payload.invoice_payment_cash_flow_changed"),

    CUSTOMER_CREATED("customer_created"),
    CUSTOMER_DELETED("customer_deleted"),
    CUSTOMER_READY("customer_status_changed.status.ready"),
    CUSTOMER_BINDING_STARTED("customer_binding_changed.payload.started"),
    CUSTOMER_BINDING_SUCCEEDED("customer_binding_changed.payload.status_changed.status.succeeded"),
    CUSTOMER_BINDING_FAILED("customer_binding_changed.payload.status_changed.status.failed");

    private String thriftFilterPathCoditionRule;

    EventType(String thriftFilterPathCoditionRule) {
        this.thriftFilterPathCoditionRule = thriftFilterPathCoditionRule;
    }

    public String getThriftFilterPathCoditionRule() {
        return thriftFilterPathCoditionRule;
    }

    public boolean isInvoiceEvent(){
        return this.name().startsWith("INVOICE");
    }

    public boolean isCustomerEvent(){
        return this.name().startsWith("CUSTOMER");
    }
}
