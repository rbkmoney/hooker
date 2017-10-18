package com.rbkmoney.hooker.handler.poller.impl.customer;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.handler.Handler;

/**
 * Created by inalarsanukaev on 07.04.17.
 */
public abstract class AbstractCustomerEventHandler implements Handler<CustomerChange, StockEvent> {

    public static final String CUSTOMER = "customer";
    public static final String BINDING = "binding";

    @Override
    public void handle(CustomerChange c, StockEvent value) throws DaoException{
        Event event = value.getSourceEvent().getProcessingEvent();
        saveEvent(c, event);
    }

    protected abstract void saveEvent(CustomerChange cc, Event event) throws DaoException;
}
