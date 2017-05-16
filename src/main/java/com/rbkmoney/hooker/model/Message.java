package com.rbkmoney.hooker.model;

import com.rbkmoney.hooker.handler.poller.impl.AbstractInvoiceEventHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;


/**
 * Created by inalarsanukaev on 07.04.17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {
    private long id;
    private long eventId;
    private String eventTime;
    private String type;
    private String partyId;
    private EventType eventType;
    private Invoice invoice;
    private Payment payment;

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
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public Message copy(){
        return SerializationUtils.clone(this);
    }
}
