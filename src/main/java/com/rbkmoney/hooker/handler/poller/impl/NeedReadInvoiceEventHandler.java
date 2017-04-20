package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.model.Message;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by jeckep on 13.04.17.
 */
public abstract class NeedReadInvoiceEventHandler extends AbstractInvoiceEventHandler{
    @Autowired
    MessageDao messageDao;

    @Override
    protected void saveEvent(Event event) throws DaoException {
        final String invoiceId = event.getSource().getInvoice();
        //getAny any saved message for related invoice
        Message message = messageDao.getAny(invoiceId);
        if (message == null) {
            throw new DaoException("Message for invoice with id "+event.getSource().getInvoice() + " not exist");
        }
        modifyMessage(event, message);

        messageDao.create(message);
        //TODO getAny message id and write to logs
    }

    protected abstract void modifyMessage(Event event, Message message);
}
