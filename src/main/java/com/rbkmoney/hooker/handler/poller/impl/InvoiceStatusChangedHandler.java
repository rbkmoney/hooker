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
public class InvoiceStatusChangedHandler extends AbstractEventHandler {

    private EventTypeCode code = EventTypeCode.INVOICE_STATUS_CHANGED;
    private Filter filter;

    @Autowired
    InvoiceDao invoiceDao;

    public InvoiceStatusChangedHandler() {
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
        return event.getPayload().getInvoiceEvent().getInvoiceStatusChanged();
    }
}
