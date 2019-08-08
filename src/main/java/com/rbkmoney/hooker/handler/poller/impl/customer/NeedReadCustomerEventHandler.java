package com.rbkmoney.hooker.handler.poller.impl.customer;

import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.impl.CustomerDaoImpl;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.EventInfo;
import com.rbkmoney.hooker.model.EventType;
import lombok.RequiredArgsConstructor;

/**
 * Created by inalarsanukaev on 12.10.17.
 */
@RequiredArgsConstructor
public abstract class NeedReadCustomerEventHandler extends AbstractCustomerEventHandler {

    protected final CustomerDaoImpl customerDao;

    @Override
    protected void saveEvent(CustomerChange cc, EventInfo eventInfo) throws DaoException {
        //getAny any saved message for related invoice
        CustomerMessage message = getCustomerMessage(eventInfo.getSourceId());
        if (message == null) {
            throw new DaoException("CustomerMessage for customer with id " + eventInfo.getSourceId() + " not exist");
        }
        message.setEventType(getEventType());
        message.setType(getMessageType());
        message.setEventId(eventInfo.getEventId());
        message.setOccuredAt(eventInfo.getEventCreatedAt());
        message.setSequenceId(eventInfo.getSequenceId());
        message.setChangeId(eventInfo.getChangeId());
        modifyMessage(cc, message);

        customerDao.create(message);
    }

    protected CustomerMessage getCustomerMessage(String customerId) {
        return customerDao.getAny(customerId, getMessageType());
    }

    protected abstract String getMessageType();

    protected abstract EventType getEventType();

    protected abstract void modifyMessage(CustomerChange cc, CustomerMessage message);
}
