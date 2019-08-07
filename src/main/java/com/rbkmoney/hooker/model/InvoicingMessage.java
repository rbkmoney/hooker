package com.rbkmoney.hooker.model;

import com.rbkmoney.hooker.handler.poller.impl.invoicing.AbstractInvoiceEventHandler;
import com.rbkmoney.swag_webhook_events.model.PaymentError;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;


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

    public InvoicingMessage copy(){
        InvoicingMessage copied = new InvoicingMessage();
        BeanUtils.copyProperties(this, copied);
        copied.setInvoice(new Invoice());
        BeanUtils.copyProperties(this.getInvoice(), copied.getInvoice());
        if (this.getPayment() != null) {
            copied.setPayment(new Payment());
            BeanUtils.copyProperties(this.getPayment(), copied.getPayment());
            if (this.getPayment().getError() != null) {
                copied.getPayment().setError(new PaymentError());
                BeanUtils.copyProperties(this.getPayment().getError(), copied.getPayment().getError());
            }
        }
        if (this.getRefund() != null) {
            copied.setRefund(new Refund());
            BeanUtils.copyProperties(this.getRefund(), copied.getRefund());
            if (this.getRefund().getError() != null) {
                copied.getRefund().setError(new PaymentError());
                BeanUtils.copyProperties(this.getRefund().getError(), copied.getRefund().getError());
            }
        }
        return copied;
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
