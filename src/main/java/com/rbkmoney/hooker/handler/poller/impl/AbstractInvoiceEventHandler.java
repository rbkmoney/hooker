package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.InvoiceDao;
import com.rbkmoney.hooker.dao.InvoiceInfo;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by inalarsanukaev on 07.04.17.
 */
public abstract class AbstractInvoiceEventHandler extends AbstractEventHandler<InvoiceInfo>{
    @Autowired
    InvoiceDao invoiceDao;

    @Override
    protected InvoiceInfo getEventForPost(Event event) throws DaoException {
        InvoiceInfo invoiceInfo = invoiceDao.get(event.getSource().getInvoice());
        if (invoiceInfo == null) {
            throw new DaoException("Invoice with id "+event.getSource().getInvoice() + " not exist");
        }
        prepareInvoiceInfo(event, invoiceInfo);
        return invoiceInfo;
    }

    @Override
    protected String getPartyId(InvoiceInfo eventForPost) {
        return eventForPost.getPartyId();
    }

    protected abstract void prepareInvoiceInfo(Event event, InvoiceInfo invoiceInfo);
}
