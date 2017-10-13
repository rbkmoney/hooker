package com.rbkmoney.hooker.handler.poller.impl.customer;

import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.hooker.dao.DaoException;

/**
 * Created by inalarsanukaev on 12.10.17.
 */
public abstract class NeedReadCustomerEventHandler extends AbstractCustomerEventHandler {
    @Override
    protected void saveEvent(CustomerChange cc, Event event) throws DaoException {
        event.getSource().getCustomerId();
    }
}
