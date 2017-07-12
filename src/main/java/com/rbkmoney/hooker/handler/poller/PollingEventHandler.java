package com.rbkmoney.hooker.handler.poller;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.hooker.handler.Handler;

public interface PollingEventHandler extends Handler<InvoiceChange, StockEvent> {

}
