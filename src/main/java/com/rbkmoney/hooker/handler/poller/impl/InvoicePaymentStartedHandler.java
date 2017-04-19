package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.domain.InvoicePayment;
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
public class InvoicePaymentStartedHandler extends AbstractInvoiceEventHandler {
    @Autowired
    InvoiceDao invoiceDao;

    private Filter filter;
    private EventTypeCode code = EventTypeCode.INVOICE_PAYMENT_STARTED;

    public InvoicePaymentStartedHandler() {
        filter = new PathConditionFilter(new PathConditionRule(code.getKey()));
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    protected List<Webhook> getWebhooks(InvoiceInfo invoiceInfo) {
        return webhookDao.getWebhooksForInvoices(code, invoiceInfo.getPartyId(), invoiceInfo.getShopId());
    }

    @Override
    protected void prepareInvoiceInfo(Event event, InvoiceInfo invoiceInfo) {
        invoiceInfo.setDescription("Создание платежа");
        InvoicePayment payment = event.getPayload().getInvoiceEvent().getInvoicePaymentEvent().getInvoicePaymentStarted().getPayment();
        invoiceInfo.setStatus(payment.getStatus().getSetField().getFieldName());
        invoiceInfo.setEventType("payment");
        invoiceInfo.setPaymentId(payment.getId());
    }
}
