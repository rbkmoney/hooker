package com.rbkmoney.hooker.handler.poller.customer;

import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.hooker.exception.DaoException;
import com.rbkmoney.hooker.handler.Handler;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.EventInfo;

/**
 * Created by inalarsanukaev on 07.04.17.
 */
public abstract class AbstractCustomerEventHandler implements Handler<CustomerChange, CustomerMessage> {

    @Override
    public void handle(CustomerChange c, EventInfo eventInfo) throws DaoException {
        saveEvent(c, eventInfo);
    }

    protected abstract void saveEvent(CustomerChange cc, EventInfo eventInfo) throws DaoException;
}
