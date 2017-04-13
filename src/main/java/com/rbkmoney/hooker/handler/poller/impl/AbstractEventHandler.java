package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.handler.PollingException;
import com.rbkmoney.hooker.handler.poller.PollingEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by inalarsanukaev on 06.04.17.
 */
public abstract class AbstractEventHandler implements PollingEventHandler<StockEvent> {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void handle(StockEvent value) throws PollingException {
        Event event = value.getSourceEvent().getProcessingEvent();
        saveEvent(event);
    }

    protected abstract void saveEvent(Event event) throws DaoException;
}
