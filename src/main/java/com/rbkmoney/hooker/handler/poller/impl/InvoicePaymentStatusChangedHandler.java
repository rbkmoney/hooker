package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.hooker.dao.EventTypeCode;
import com.rbkmoney.hooker.dao.InvoiceDao;
import com.rbkmoney.thrift.filter.Filter;
import com.rbkmoney.thrift.filter.PathConditionFilter;
import com.rbkmoney.thrift.filter.rule.PathConditionRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoicePaymentStatusChangedHandler extends AbstractEventHandler {
    @Autowired
    InvoiceDao invoiceDao;

    private Filter filter;
    private EventTypeCode code = EventTypeCode.INVOICE_PAYMENT_STATUS_CHANGED;

    public InvoicePaymentStatusChangedHandler() {
        filter = new PathConditionFilter(new PathConditionRule(code.getKey()));
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    protected EventTypeCode getCode() {
        return code;
    }

    @Override
    protected String getPartyId(Event event) throws Exception {
        return invoiceDao.getParty(event.getSource().getInvoice());
    }

    @Override
    protected Object getEventForPost(Event event) {
        return event.getPayload().getInvoiceEvent().getInvoicePaymentEvent().getInvoicePaymentStatusChanged();
    }
}
