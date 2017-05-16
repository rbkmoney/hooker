package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.domain.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.model.Payment;
import com.rbkmoney.hooker.model.PaymentContactInfo;
import com.rbkmoney.thrift.filter.Filter;
import com.rbkmoney.thrift.filter.PathConditionFilter;
import com.rbkmoney.thrift.filter.rule.PathConditionRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoicePaymentStartedHandler extends NeedReadInvoiceEventHandler {
    @Autowired
    MessageDao messageDao;

    private Filter filter;
    private EventType eventType = EventType.INVOICE_PAYMENT_STARTED;

    public InvoicePaymentStartedHandler() {
        filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule()));
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    protected void modifyMessage(Event event, Message message) {
        message.setEventType(eventType);
        message.setType(PAYMENT);
        InvoicePayment paymentOrigin = event.getPayload().getInvoiceEvent().getInvoicePaymentEvent().getInvoicePaymentStarted().getPayment();
        Payment payment = new Payment();
        message.setPayment(payment);
        payment.setId(paymentOrigin.getId());
        payment.setCreatedAt(paymentOrigin.getCreatedAt());
        payment.setStatus(paymentOrigin.getStatus().getSetField().getFieldName());
        payment.setAmount(paymentOrigin.getCost().getAmount());
        payment.setCurrency(paymentOrigin.getCost().getCurrency().getSymbolicCode());
        payment.setPaymentToolToken(paymentOrigin.getPayer().getPaymentTool().getBankCard().getToken());
        payment.setPaymentSession(paymentOrigin.getPayer().getSession());
        payment.setContactInfo(new PaymentContactInfo(paymentOrigin.getPayer().getContactInfo().getEmail(), paymentOrigin.getPayer().getContactInfo().getPhoneNumber()));
        payment.setIp(paymentOrigin.getPayer().getClientInfo().getIpAddress());
        payment.setFingerprint(paymentOrigin.getPayer().getClientInfo().getFingerprint());
    }
}
