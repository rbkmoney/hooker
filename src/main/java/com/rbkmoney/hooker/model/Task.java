package com.rbkmoney.hooker.model;

/**
 * Created by jeckep on 17.04.17.
 */
public class Task {
    long hookId;
    long messageId;
    String invoiceId;

    public Task(long hookId, long messageId, String invoiceId) {
        this.hookId = hookId;
        this.messageId = messageId;
        this.invoiceId = invoiceId;
    }

    public long getHookId() {
        return hookId;
    }

    public void setHookId(long hookId) {
        this.hookId = hookId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }
}
