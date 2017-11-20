package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.InvoicingMessage;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by jeckep on 13.04.17.
 */
public abstract class NeedReadInvoiceEventHandler extends AbstractInvoiceEventHandler{
    @Autowired
    InvoicingMessageDao messageDao;

    @Override
    protected void saveEvent(InvoiceChange ic, Event event) throws DaoException {
        final String invoiceId = event.getSource().getInvoiceId();
        //getAny any saved message for related invoice
        InvoicingMessage message = getMessage(invoiceId);
        if (message == null) {
            throw new DaoException("InvoicingMessage for invoice with id " + invoiceId + " not exist");
        }
        message.setEventType(getEventType());
        message.setType(getMessageType());
        message.setEventId(event.getId());
        message.setEventTime(event.getCreatedAt());
        modifyMessage(ic, event, message);

        messageDao.create(message);
        //TODO getAny message id and write to logs
    }

    protected InvoicingMessage getMessage(String invoiceId) {
        return messageDao.getAny(invoiceId, getMessageType());
    }

    protected abstract String getMessageType();

    protected abstract EventType getEventType();

    protected abstract void modifyMessage(InvoiceChange ic, Event event, InvoicingMessage message);
}
