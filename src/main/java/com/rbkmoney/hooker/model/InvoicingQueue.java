package com.rbkmoney.hooker.model;

/**
 * Created by inalarsanukaev on 14.11.17.
 */
public class InvoicingQueue extends Queue {
    private String invoiceId;

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

}
