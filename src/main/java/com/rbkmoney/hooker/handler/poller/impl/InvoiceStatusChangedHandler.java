package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.webhooker.Webhook;
import com.rbkmoney.hooker.dao.EventTypeCode;
import com.rbkmoney.hooker.dao.InvoiceDao;
import com.rbkmoney.hooker.dao.InvoiceInfo;
import com.rbkmoney.thrift.filter.Filter;
import com.rbkmoney.thrift.filter.PathConditionFilter;
import com.rbkmoney.thrift.filter.rule.PathConditionRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvoiceStatusChangedHandler extends AbstractInvoiceEventHandler {

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
    protected List<Webhook> getWebhooks(InvoiceInfo eventForPost) {
        return webhookDao.getWebhooksForInvoiceStatusChanged(code, eventForPost.getPartyId(), eventForPost.getShopId(), eventForPost.getStatus());
    }

    @Override
    protected void prepareInvoiceInfo(Event event, InvoiceInfo invoiceInfo) {
        invoiceInfo.setDescription("Изменение статуса инвойса");
        invoiceInfo.setStatus(event.getPayload().getInvoiceEvent().getInvoiceStatusChanged().getStatus().getSetField().getFieldName());
        invoiceInfo.setEventType("invoice");
    }
}
