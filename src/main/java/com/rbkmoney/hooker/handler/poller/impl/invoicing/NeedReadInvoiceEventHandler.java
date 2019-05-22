package com.rbkmoney.hooker.handler.poller.impl.invoicing;

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
    protected void saveEvent(InvoiceChange ic, Long eventId, String eventCreatedAt, String sourceId, Long sequenceId, Integer changeId) throws DaoException {
        //getAny any saved message for related invoice
        InvoicingMessage message = getMessage(sourceId, ic);
        if (message == null) {
            throw new DaoException("InvoicingMessage for invoice with id " + sourceId + " not exist");
        }
        message.setEventType(getEventType());
        message.setType(getMessageType());
        message.setEventId(eventId);
        message.setEventTime(eventCreatedAt);
        message.setSequenceId(sequenceId);
        message.setChangeId(changeId);
        modifyMessage(ic, message);
        if (!messageDao.updateIfExists(message)) {
            messageDao.create(message);
        }
    }

    protected abstract InvoicingMessage getMessage(String invoiceId, InvoiceChange ic);

    protected abstract String getMessageType();

    protected abstract EventType getEventType();

    protected abstract void modifyMessage(InvoiceChange ic, InvoicingMessage message);


}
