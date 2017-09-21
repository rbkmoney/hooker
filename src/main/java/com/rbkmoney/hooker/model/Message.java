package com.rbkmoney.hooker.model;

import com.rbkmoney.hooker.handler.poller.impl.AbstractInvoiceEventHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Created by inalarsanukaev on 07.04.17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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
