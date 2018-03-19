package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.handler.Handler;

/**
 * Created by inalarsanukaev on 07.04.17.
 */
public abstract class AbstractInvoiceEventHandler implements Handler<InvoiceChange, StockEvent> {

    public static final String INVOICE = "invoice";
    public static final String PAYMENT = "payment";
    public static final String REFUND  = "refund";

    @Override
    public void handle(InvoiceChange ic, StockEvent value) throws DaoException{
        Event event = value.getSourceEvent().getProcessingEvent();
        saveEvent(ic, event);
    }

    protected abstract void saveEvent(InvoiceChange ic, Event event) throws DaoException;
}
