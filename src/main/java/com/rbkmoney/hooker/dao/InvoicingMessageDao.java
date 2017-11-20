package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.model.InvoicingMessage;

public interface InvoicingMessageDao extends MessageDao<InvoicingMessage> {
    InvoicingMessage getAny(String invoiceId, String paymentType) throws DaoException;
}
