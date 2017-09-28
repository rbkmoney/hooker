package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.handler.poller.PollingEventHandler;

/**
 * Created by inalarsanukaev on 07.04.17.
 */
public abstract class AbstractInvoiceEventHandler implements PollingEventHandler {

    public static final String INVOICE = "invoice";
    public static final String PAYMENT = "payment";

    @Override
    public void handle(InvoiceChange ic, StockEvent value) throws DaoException{
        Event event = value.getSourceEvent().getProcessingEvent();
        saveEvent(ic, event);
    }

    protected abstract void saveEvent(InvoiceChange ic, Event event) throws DaoException;
}
