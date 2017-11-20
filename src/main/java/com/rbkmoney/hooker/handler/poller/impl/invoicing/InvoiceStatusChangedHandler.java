package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.domain.InvoiceStatus;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.InvoicingMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoiceStatusChangedHandler extends NeedReadInvoiceEventHandler {

    private EventType eventType = EventType.INVOICE_STATUS_CHANGED;
    private Filter filter;

    @Autowired
    InvoicingMessageDao messageDao;

    public InvoiceStatusChangedHandler() {
        filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule(), new IsNullCondition().not()));
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    protected String getMessageType() {
        return INVOICE;
    }

    @Override
    protected EventType getEventType() {
        return eventType;
    }

    @Override
    protected void modifyMessage(InvoiceChange ic, Event event, InvoicingMessage message) {
        InvoiceStatus statusOrigin = ic.getInvoiceStatusChanged().getStatus();
        message.getInvoice().setStatus(statusOrigin.getSetField().getFieldName());
        if (statusOrigin.isSetCancelled()) {
            message.getInvoice().setReason(statusOrigin.getCancelled().getDetails());
        } else if (statusOrigin.isSetFulfilled()) {
            message.getInvoice().setReason(statusOrigin.getFulfilled().getDetails());
        }
    }
}
