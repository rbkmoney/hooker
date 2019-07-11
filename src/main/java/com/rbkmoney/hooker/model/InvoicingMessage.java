package com.rbkmoney.hooker.model;

import com.rbkmoney.hooker.handler.poller.impl.invoicing.AbstractInvoiceEventHandler;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Created by inalarsanukaev on 07.04.17.
 */
@NoArgsConstructor
@Getter
@Setter
public class InvoicingMessage extends Message {
    private Long eventId;
    private Long sequenceId;
    private Integer changeId;
    private String eventTime;
    private String type;
    private String partyId;
    private EventType eventType;
    private Invoice invoice;
    private Payment payment;
    private Refund refund;

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
}
