package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.handler.Handler;
import com.rbkmoney.hooker.model.InvoicingMessage;

/**
 * Created by inalarsanukaev on 07.04.17.
 */
public abstract class AbstractInvoiceEventHandler implements Handler<InvoiceChange, InvoicingMessage> {

    public static final String INVOICE = "invoice";
    public static final String PAYMENT = "payment";
    public static final String REFUND  = "refund";

    @Override
    public InvoicingMessage handle(InvoiceChange ic, Long eventId, String eventCreatedAt, String sourceId, Long sequenceId, Integer changeId) throws DaoException {
        return buildEvent(ic, eventId, eventCreatedAt, sourceId, sequenceId, changeId);
    }

    protected abstract InvoicingMessage buildEvent(InvoiceChange ic, Long eventId, String eventCreatedAt, String sourceId, Long sequenceId, Integer changeId) throws DaoException;
}
