package com.rbkmoney.hooker.model;

import com.rbkmoney.hooker.handler.poller.impl.invoicing.AbstractInvoiceEventHandler;


/**
 * Created by inalarsanukaev on 07.04.17.
 */
public class Message {
    private long id;
    private long eventId;
    private String eventTime;
    private String type;
    private String partyId;
    private EventType eventType;
    private Invoice invoice;
    private Payment payment;

    public Message(Message other) {
        this.id = other.id;
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
    }

    public Message(long id, long eventId, String eventTime, String type, String partyId, EventType eventType, Invoice invoice, Payment payment) {
        this.id = id;
        this.eventId = eventId;
        this.eventTime = eventTime;
        this.type = type;
        this.partyId = partyId;
        this.eventType = eventType;
        this.invoice = invoice;
        this.payment = payment;
    }

    public Message() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public boolean isInvoice() {
        return AbstractInvoiceEventHandler.INVOICE.equals(getType());
    }

    public boolean isPayment() {
        return AbstractInvoiceEventHandler.PAYMENT.equals(getType());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        return id == message.id;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
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
        return (int) (id ^ (id >>> 32));
    }

    public Message copy(){
        return new Message(this);
    }
}
