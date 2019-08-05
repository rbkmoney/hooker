package com.rbkmoney.hooker.handler.poller.impl.customer;

import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.handler.Handler;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.machinegun.eventsink.MachineEvent;

/**
 * Created by inalarsanukaev on 07.04.17.
 */
public abstract class AbstractCustomerEventHandler implements Handler<CustomerChange, CustomerMessage> {

    public static final String CUSTOMER = "customer";
    public static final String BINDING = "binding";

    @Override
    public CustomerMessage handle(CustomerChange c, Long eventId, String eventCreatedAt, String sourceId, Long sequenceId, Integer changeId) throws DaoException{
        return saveEvent(c, eventId, eventCreatedAt, sourceId, sequenceId, changeId);
    }

    protected abstract CustomerMessage saveEvent(CustomerChange cc, Long eventId, String eventCreatedAt, String sourceId, Long sequenceId, Integer changeId) throws DaoException;
}
