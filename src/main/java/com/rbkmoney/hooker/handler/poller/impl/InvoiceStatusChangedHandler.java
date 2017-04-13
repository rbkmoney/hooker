package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.dao.InvoiceDao;
import com.rbkmoney.hooker.dao.InvoiceInfo;
import com.rbkmoney.thrift.filter.Filter;
import com.rbkmoney.thrift.filter.PathConditionFilter;
import com.rbkmoney.thrift.filter.rule.PathConditionRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoiceStatusChangedHandler extends AbstractInvoiceEventHandler {

    private EventType eventType = EventType.INVOICE_STATUS_CHANGED;
    private Filter filter;

    @Autowired
    InvoiceDao invoiceDao;

    public InvoiceStatusChangedHandler() {
        filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule()));
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    protected EventType getEventType() {
        return eventType;
    }

    @Override
    protected void prepareInvoiceInfo(Event event, InvoiceInfo invoiceInfo) {
        invoiceInfo.setDescription("Изменение статуса инвойса");
        invoiceInfo.setStatus(event.getPayload().getInvoiceEvent().getInvoiceStatusChanged().getStatus().getSetField().getFieldName());
        invoiceInfo.setEventType("invoice");
    }
}
