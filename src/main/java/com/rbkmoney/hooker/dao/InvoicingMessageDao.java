package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.exception.DaoException;
import com.rbkmoney.hooker.exception.NotFoundException;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.InvoicingMessageKey;

public interface InvoicingMessageDao extends MessageDao<InvoicingMessage> {
    InvoicingMessage getInvoicingMessage(InvoicingMessageKey key) throws NotFoundException, DaoException;
}
