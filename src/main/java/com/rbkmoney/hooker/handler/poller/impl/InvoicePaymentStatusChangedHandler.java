package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentStatusChanged;
import com.rbkmoney.hooker.dao.EventTypeCode;
import com.rbkmoney.hooker.dao.InvoiceDao;
import com.rbkmoney.hooker.dao.InvoiceInfo;
import com.rbkmoney.thrift.filter.Filter;
import com.rbkmoney.thrift.filter.PathConditionFilter;
import com.rbkmoney.thrift.filter.rule.PathConditionRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoicePaymentStatusChangedHandler extends AbstractInvoiceEventHandler {
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
    protected void prepareInvoiceInfo(Event event, InvoiceInfo invoiceInfo) {
        invoiceInfo.setDescription("Изменение статуса платежа");
        InvoicePaymentStatusChanged payment = event.getPayload().getInvoiceEvent().getInvoicePaymentEvent().getInvoicePaymentStatusChanged();
        invoiceInfo.setStatus(payment.getStatus().getSetField().getFieldName());
        invoiceInfo.setEventType("payment");
        invoiceInfo.setPaymentId(payment.getPaymentId());
    }
}
