package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.dao.NotFoundException;
import com.rbkmoney.hooker.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public abstract class NeedReadInvoiceEventMapper extends AbstractInvoiceEventMapper {

    private final InvoicingMessageDao messageDao;

    @Override
    protected InvoicingMessage buildEvent(InvoiceChange ic, EventInfo eventInfo, Map<InvoicingMessageKey, InvoicingMessage> storage) throws DaoException {
        InvoicingMessage message;
        InvoicingMessageKey messageKey = getMessageKey(eventInfo.getSourceId(), ic);
        try {
            message = storage.get(messageKey);
            if (message == null) {
                message = messageDao.getInvoicingMessage(messageKey);
            }
            message = message.copy();
        } catch (NotFoundException e) {
            log.warn(e.getMessage());
            return null;
        }
        message.setEventType(getEventType());
        message.setType(getMessageType().value());
        message.setEventTime(eventInfo.getEventCreatedAt());
        message.setSequenceId(eventInfo.getSequenceId());
        message.setChangeId(eventInfo.getChangeId());
        modifyMessage(ic, message);
        return message;
    }

    protected abstract InvoicingMessageEnum getMessageType();

    protected abstract InvoicingMessageKey getMessageKey(String invoiceId, InvoiceChange ic) throws NotFoundException, DaoException;

    protected abstract EventType getEventType();

    protected abstract void modifyMessage(InvoiceChange ic, InvoicingMessage message);
}
