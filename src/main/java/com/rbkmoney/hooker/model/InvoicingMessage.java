package com.rbkmoney.hooker.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.BeanUtils;


/**
 * Created by inalarsanukaev on 07.04.17.
 */
@NoArgsConstructor
@Data
@ToString
public class InvoicingMessage extends Message {
    private InvoicingMessageEnum type;
    private String invoiceId;
    private InvoiceStatusEnum invoiceStatus;
    private String paymentId;
    private PaymentStatusEnum paymentStatus;
    private Long paymentFee;
    private String refundId;
    private RefundStatusEnum refundStatus;

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
