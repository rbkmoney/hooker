package com.rbkmoney.hooker.handler.poller.impl.customer;

import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.hooker.dao.CustomerDao;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.EventType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by inalarsanukaev on 12.10.17.
 */
public abstract class NeedReadCustomerEventHandler extends AbstractCustomerEventHandler {

    @Autowired
    CustomerDao customerDao;

    @Override
    protected void saveEvent(CustomerChange cc, Event event) throws DaoException {
        final String customerId = event.getSource().getCustomerId();
        //getAny any saved message for related invoice
        CustomerMessage message = getCustomerMessage(customerId);
        if (message == null) {
            throw new DaoException("CustomerMessage for customer with id " + customerId + " not exist");
        }
        message.setEventType(getEventType());
        message.setType(getMessageType());
        message.setEventId(event.getId());
        message.setOccuredAt(event.getCreatedAt());
        modifyMessage(cc, event, message);

        customerDao.create(message);
    }

    protected CustomerMessage getCustomerMessage(String customerId) {
        return customerDao.getAny(customerId, getMessageType());
    }

    protected abstract String getMessageType();

    protected abstract EventType getEventType();

    protected abstract void modifyMessage(CustomerChange cc, Event event, CustomerMessage message);
}
