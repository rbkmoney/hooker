package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.payment_processing.Invoice;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.hooker.model.Message;

public interface HellgateInvoicingService<M extends Message> {

    Invoice getInvoiceByMessage(M message);

    InvoicePayment getPaymentByMessage(M message);

}
