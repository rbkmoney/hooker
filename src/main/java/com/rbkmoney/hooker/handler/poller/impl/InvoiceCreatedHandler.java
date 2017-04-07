package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.hooker.dao.EventTypeCode;
import com.rbkmoney.hooker.dao.InvoiceDao;
import com.rbkmoney.thrift.filter.Filter;
import com.rbkmoney.thrift.filter.PathConditionFilter;
import com.rbkmoney.thrift.filter.rule.PathConditionRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoiceCreatedHandler extends AbstractEventHandler {

    @Autowired
    InvoiceDao invoiceDao;

    private Filter filter;

    private EventTypeCode code = EventTypeCode.INVOICE_CREATED;

    public InvoiceCreatedHandler() {
        filter = new PathConditionFilter(new PathConditionRule(code.getKey()));
    }

    @Override
    public void handle(StockEvent value) throws Exception {
        super.handle(value);
        Event event = value.getSourceEvent().getProcessingEvent();
        String invoiceId = event.getSource().getInvoice();
        invoiceDao.add(event.getPayload().getInvoiceEvent().getInvoiceCreated().getInvoice().getOwnerId(), invoiceId);
    }

    @Override
    protected EventTypeCode getCode() {
        return code;
    }

    @Override
    protected String getPartyId(Event event) {
        return event.getPayload().getInvoiceEvent().getInvoiceCreated().getInvoice().getOwnerId();
    }

    @Override
    protected Object getEventForPost(Event event) {
        return event.getPayload().getInvoiceEvent().getInvoiceCreated();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
