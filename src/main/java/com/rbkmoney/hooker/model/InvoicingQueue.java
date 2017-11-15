package com.rbkmoney.hooker.model;

/**
 * Created by inalarsanukaev on 14.11.17.
 */
public class InvoicingQueue {
    private long id;
    private long hookId;
    private String invoiceId;

    public InvoicingQueue(long id, long hookId, String invoiceId) {
        this.id = id;
        this.hookId = hookId;
        this.invoiceId = invoiceId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getHookId() {
        return hookId;
    }

    public void setHookId(long hookId) {
        this.hookId = hookId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }
}
