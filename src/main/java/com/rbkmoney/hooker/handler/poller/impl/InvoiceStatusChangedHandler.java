package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.domain.InvoiceStatus;
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
        InvoiceStatus statusOrigin = event.getPayload().getInvoiceEvent().getInvoiceStatusChanged().getStatus();
        message.getInvoice().setStatus(statusOrigin.getSetField().getFieldName());
        if (statusOrigin.isSetCancelled()) {
            message.getInvoice().setReason(statusOrigin.getCancelled().getDetails());
        } else if (statusOrigin.isSetFulfilled()) {
            message.getInvoice().setReason(statusOrigin.getFulfilled().getDetails());
        }
        message.setType(INVOICE);
        message.setEventType(eventType);
    }
}
