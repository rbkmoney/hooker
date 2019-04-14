package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.handler.Handler;
import com.rbkmoney.machinegun.eventsink.MachineEvent;

/**
 * Created by inalarsanukaev on 07.04.17.
 */
public abstract class AbstractInvoiceEventHandler implements Handler<InvoiceChange, MachineEvent> {

    public static final String INVOICE = "invoice";
    public static final String PAYMENT = "payment";
    public static final String REFUND  = "refund";

    @Override
    public void handle(InvoiceChange ic, MachineEvent event) throws DaoException {
        saveEvent(ic, event);
    }

    protected abstract void saveEvent(InvoiceChange ic, MachineEvent event) throws DaoException;
}
