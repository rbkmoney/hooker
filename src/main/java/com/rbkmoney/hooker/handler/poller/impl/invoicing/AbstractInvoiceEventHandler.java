package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.handler.Handler;

/**
 * Created by inalarsanukaev on 07.04.17.
 */
public abstract class AbstractInvoiceEventHandler implements Handler<InvoiceChange> {

    public static final String INVOICE = "invoice";
    public static final String PAYMENT = "payment";
    public static final String REFUND  = "refund";

    @Override
    public void handle(InvoiceChange ic, Long eventId, String eventCreatedAt, String sourceId, Long sequenceId, Integer changeId) throws DaoException {
        saveEvent(ic, eventId, eventCreatedAt, sourceId, sequenceId, changeId);
    }

    protected abstract void saveEvent(InvoiceChange ic, Long eventId, String eventCreatedAt, String sourceId, Long sequenceId, Integer changeId) throws DaoException;
}
