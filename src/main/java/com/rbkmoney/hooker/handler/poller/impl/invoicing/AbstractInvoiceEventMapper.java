package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.NotFoundException;
import com.rbkmoney.hooker.handler.Handler;
import com.rbkmoney.hooker.handler.Mapper;
import com.rbkmoney.hooker.model.EventInfo;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.InvoicingMessageKey;

import java.util.Map;

/**
 * Created by inalarsanukaev on 07.04.17.
 */
public abstract class AbstractInvoiceEventMapper implements Mapper<InvoiceChange, InvoicingMessage> {

    @Override
    public InvoicingMessage handle(InvoiceChange ic, EventInfo eventInfo, Map<InvoicingMessageKey, InvoicingMessage> storage) throws DaoException {
        InvoicingMessage message = buildEvent(ic, eventInfo, storage);
        storage.put(getMessageKey(eventInfo.getSourceId(), ic), message);
        return message;
    }

    protected abstract InvoicingMessageKey getMessageKey(String invoiceId, InvoiceChange ic) throws NotFoundException, DaoException;

    protected abstract InvoicingMessage buildEvent(InvoiceChange ic, EventInfo eventInfo, Map<InvoicingMessageKey, InvoicingMessage> storage) throws DaoException;
}
