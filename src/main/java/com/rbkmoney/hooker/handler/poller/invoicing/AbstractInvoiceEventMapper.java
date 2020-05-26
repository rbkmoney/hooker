package com.rbkmoney.hooker.handler.poller.invoicing;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.hooker.exception.DaoException;
import com.rbkmoney.hooker.handler.Mapper;
import com.rbkmoney.hooker.model.EventInfo;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.InvoicingMessageKey;

import java.util.Map;

public abstract class AbstractInvoiceEventMapper implements Mapper<InvoiceChange, InvoicingMessage> {

    @Override
    public InvoicingMessage handle(InvoiceChange ic, EventInfo eventInfo, Map<InvoicingMessageKey, InvoicingMessage> storage) throws DaoException {
        return buildEvent(ic, eventInfo, storage);
    }

    protected abstract InvoicingMessage buildEvent(InvoiceChange ic, EventInfo eventInfo, Map<InvoicingMessageKey, InvoicingMessage> storage) throws DaoException;
}
