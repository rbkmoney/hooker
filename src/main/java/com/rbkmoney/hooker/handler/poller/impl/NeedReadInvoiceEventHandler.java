package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Message;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by jeckep on 13.04.17.
 */
public abstract class NeedReadInvoiceEventHandler extends AbstractInvoiceEventHandler{
    @Autowired
    MessageDao messageDao;

    @Override
    protected void saveEvent(InvoiceChange ic, Event event) throws DaoException {
        final String invoiceId = event.getSource().getInvoiceId();
        //getAny any saved message for related invoice
        Message message = getMessage(invoiceId);
        if (message == null) {
            throw new DaoException("Message for invoice with id " + invoiceId + " not exist");
        }
        message.setEventType(getEventType());
        message.setType(getMessageType());
        message.setEventId(event.getId());
        message.setEventTime(event.getCreatedAt());
        modifyMessage(ic, event, message);

        messageDao.create(message);
        //TODO getAny message id and write to logs
    }

    protected Message getMessage(String invoiceId) {
        return messageDao.getAny(invoiceId, getMessageType());
    }

    protected abstract String getMessageType();

    protected abstract EventType getEventType();

    protected abstract void modifyMessage(InvoiceChange ic, Event event, Message message);
}
