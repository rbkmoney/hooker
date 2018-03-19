package com.rbkmoney.hooker.model;

import com.rbkmoney.hooker.handler.poller.impl.invoicing.AbstractInvoiceEventHandler;


/**
 * Created by inalarsanukaev on 07.04.17.
 */
public class InvoicingMessage extends Message {
    private long eventId;
    private String eventTime;
    private String type;
    private String partyId;
    private EventType eventType;
    private Invoice invoice;
    private Payment payment;
    private Refund refund;

    public InvoicingMessage(InvoicingMessage other) {
        setId(other.getId());
        this.eventId = other.eventId;
        this.eventTime = other.eventTime;
        this.type = other.type;
        this.partyId = other.partyId;
        this.eventType = other.eventType;
        if (other.invoice != null) {
            this.invoice = new Invoice(other.invoice);
        }
        if (other.payment != null) {
            this.payment = new Payment(other.payment);
        }
        if (other.refund != null) {
            this.refund = new Refund(other.refund);
        }
    }

    public InvoicingMessage() {
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Refund getRefund() {
        return refund;
    }

    public void setRefund(Refund refund) {
        this.refund = refund;
    }

    public boolean isInvoice() {
        return AbstractInvoiceEventHandler.INVOICE.equals(getType());
    }

    public boolean isPayment() {
        return AbstractInvoiceEventHandler.PAYMENT.equals(getType());
    }

    public boolean isRefund() {
        return AbstractInvoiceEventHandler.REFUND.equals(getType());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InvoicingMessage message = (InvoicingMessage) o;

        return getId() == message.getId();
    }

    @Override
    public String toString() {
        return "InvoicingMessage{" +
                "id=" + getId() +
                ", eventId=" + eventId +
                ", eventTime='" + eventTime + '\'' +
                ", invoiceId=" + invoice.getId() +
                ", invoiceStatus=" + invoice.getStatus() +
                (isPayment() ? ", paymentId=" + payment.getId() : "") +
                (isPayment() ? ", paymentStatus=" + payment.getStatus() : "") +
                '}';
    }

    @Override
    public int hashCode() {
        return (int) (getId() ^ (getId() >>> 32));
    }

    public InvoicingMessage copy(){
        return new InvoicingMessage(this);
    }
}
