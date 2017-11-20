package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.model.InvoicingMessage;

import java.util.Collection;
import java.util.List;

public interface InvoicingMessageDao {
    InvoicingMessage getAny(String invoiceId, String type) throws DaoException;
    InvoicingMessage create(InvoicingMessage message) throws DaoException;
    Long getMaxEventId();
    List<InvoicingMessage> getBy(Collection<Long> messageIds);
}
