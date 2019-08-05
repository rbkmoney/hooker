package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.NotFoundException;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.InvoicingMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class NeedReadInvoiceEventHandler extends AbstractInvoiceEventHandler{

    @Override
    protected InvoicingMessage buildEvent(InvoiceChange ic, Long eventId, String eventCreatedAt, String sourceId, Long sequenceId, Integer changeId) throws DaoException {
        //getAny any saved message for related invoice
        InvoicingMessage message;
        try {
            message = getMessage(sourceId, ic);
        } catch (NotFoundException e) {
            log.warn(e.getMessage());
            return null; //TODO
        }
        message.setEventType(getEventType());
        message.setType(getMessageType());
        message.setEventTime(eventCreatedAt);
        message.setSequenceId(sequenceId);
        message.setChangeId(changeId);
        modifyMessage(ic, message);
        return message;
    }

    protected abstract InvoicingMessage getMessage(String invoiceId, InvoiceChange ic) throws NotFoundException, DaoException;

    protected abstract String getMessageType();

    protected abstract EventType getEventType();

    protected abstract void modifyMessage(InvoiceChange ic, InvoicingMessage message);

}
