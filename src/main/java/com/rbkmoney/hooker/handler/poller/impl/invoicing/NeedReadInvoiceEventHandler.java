package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.damsel.domain.OperationFailure;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.StatusError;
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
        InvoicingMessage message = getMessage(invoiceId, ic);
        if (message == null) {
            throw new DaoException("InvoicingMessage for invoice with id " + invoiceId + " not exist");
        }
        message.setEventType(getEventType());
        message.setType(getMessageType());
        message.setEventId(event.getId());
        message.setEventTime(event.getCreatedAt());
        modifyMessage(ic, event, message);

        messageDao.create(message);
    }

    protected abstract InvoicingMessage getMessage(String invoiceId, InvoiceChange ic);

    protected abstract String getMessageType();

    protected abstract EventType getEventType();

    protected abstract void modifyMessage(InvoiceChange ic, Event event, InvoicingMessage message);

    protected StatusError getStatusError(OperationFailure failure) {
        if (failure.isSetFailure()) {
            Failure external = failure.getFailure();
            return new StatusError(external.getCode(), external.getReason());
        } else if (failure.isSetOperationTimeout()) {
            return new StatusError("408", "Operation timeout");
        }
        return null;
    }
}
