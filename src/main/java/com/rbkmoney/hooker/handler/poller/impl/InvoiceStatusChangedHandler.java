package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.thrift.filter.Filter;
import com.rbkmoney.thrift.filter.PathConditionFilter;
import com.rbkmoney.thrift.filter.rule.PathConditionRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoiceStatusChangedHandler extends NeedReadInvoiceEventHandler {

    private EventType eventType = EventType.INVOICE_STATUS_CHANGED;
    private Filter filter;

    @Autowired
    MessageDao messageDao;

    public InvoiceStatusChangedHandler() {
        filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule()));
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    protected void modifyMessage(Event event, Message message) {
        message.setStatus(event.getPayload().getInvoiceEvent().getInvoiceStatusChanged().getStatus().getSetField().getFieldName());
        message.setType(INVOICE);
        message.setEventId(event.getId());
        message.setEventType(eventType);
    }
}
