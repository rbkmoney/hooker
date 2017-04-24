package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentStatusChanged;
import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.thrift.filter.Filter;
import com.rbkmoney.thrift.filter.PathConditionFilter;
import com.rbkmoney.thrift.filter.rule.PathConditionRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoicePaymentStatusChangedHandler extends NeedReadInvoiceEventHandler {
    @Autowired
    MessageDao messageDao;

    private Filter filter;
    private EventType eventType = EventType.INVOICE_PAYMENT_STATUS_CHANGED;

    public InvoicePaymentStatusChangedHandler() {
        filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule()));
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    protected void modifyMessage(Event event, Message message) {
        InvoicePaymentStatusChanged payment = event.getPayload().getInvoiceEvent().getInvoicePaymentEvent().getInvoicePaymentStatusChanged();
        message.setStatus(payment.getStatus().getSetField().getFieldName());
        message.setType(PAYMENT);
        message.setPaymentId(payment.getPaymentId());
        message.setEventId(event.getId());
        message.setEventType(eventType);
    }
}
