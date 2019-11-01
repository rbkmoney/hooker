package com.rbkmoney.hooker.model;

import lombok.*;
import org.springframework.beans.BeanUtils;


/**
 * Created by inalarsanukaev on 07.04.17.
 */
@NoArgsConstructor
@Data
@ToString
public class InvoicingMessage extends Message {
    private Long eventId;
    private Long sequenceId;
    private Integer changeId;
    private String eventTime;
    private InvoicingMessageEnum type;
    private String partyId;
    private String shopID;
    private EventType eventType;
    private String invoiceId;
    private InvoiceStatusEnum invoiceStatus;
    private String paymentId;
    private PaymentStatusEnum paymentStatus;
    private Long paymentFee;
    private String refundId;
    private RefundStatusEnum refundStatus;
    private Long refundAmount;
    private String refundCurrency;

    public boolean isInvoice() {
        return type == InvoicingMessageEnum.INVOICE;
    }

    public boolean isPayment() {
        return type == InvoicingMessageEnum.PAYMENT;
    }

    public boolean isRefund() {
        return type == InvoicingMessageEnum.REFUND;
    }

    public InvoicingMessage copy(){
        InvoicingMessage copied = new InvoicingMessage();
        BeanUtils.copyProperties(this, copied);
        return copied;
    }
}
