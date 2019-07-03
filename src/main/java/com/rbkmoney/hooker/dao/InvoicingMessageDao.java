package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.model.InvoicingMessage;

public interface InvoicingMessageDao extends MessageDao<InvoicingMessage> {
    InvoicingMessage getInvoice(String invoiceId) throws DaoException;
    InvoicingMessage getPayment(String invoiceId, String paymentId) throws DaoException;
    InvoicingMessage getRefund(String invoiceId, String paymentId, String refundId) throws DaoException;
}
