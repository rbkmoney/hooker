package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by jeckep on 13.04.17.
 */
public abstract class NeedReadInvoiceEventHandler extends AbstractInvoiceEventHandler{
    @Autowired
    InvoicingMessageDao messageDao;

    @Override
    protected void saveEvent(InvoiceChange ic, MachineEvent event) throws DaoException {
        final String invoiceId = event.getSourceId();
        //getAny any saved message for related invoice
        InvoicingMessage message = getMessage(invoiceId, ic);
        if (message == null) {
            throw new DaoException("InvoicingMessage for invoice with id " + invoiceId + " not exist");
        }
        message.setEventType(getEventType());
        message.setType(getMessageType());
        message.setEventId(event.getEventId());
        message.setEventTime(event.getCreatedAt());
        modifyMessage(ic, event, message);
        if (!messageDao.isDuplicate(message)) {
            messageDao.create(message);
        }
    }

    protected abstract InvoicingMessage getMessage(String invoiceId, InvoiceChange ic);

    protected abstract String getMessageType();

    protected abstract EventType getEventType();

    protected abstract void modifyMessage(InvoiceChange ic, MachineEvent event, InvoicingMessage message);


}
