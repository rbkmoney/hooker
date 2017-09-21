package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.domain.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.model.Payment;
import com.rbkmoney.hooker.model.PaymentContactInfo;
import org.springframework.stereotype.Component;

@Component
public class InvoicePaymentStartedHandler extends NeedReadInvoiceEventHandler {

    private Filter filter;
    private EventType eventType = EventType.INVOICE_PAYMENT_STARTED;

    public InvoicePaymentStartedHandler() {
        filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule(), new IsNullCondition().not()));
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    protected String getMessageType() {
        return PAYMENT;
    }

    @Override
    protected EventType getEventType() {
        return eventType;
    }

    @Override
    protected void modifyMessage(InvoiceChange ic, Event event, Message message) {
        InvoicePayment paymentOrigin = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentStarted().getPayment();
        Payment payment = new Payment();
        message.setPayment(payment);
        payment.setId(paymentOrigin.getId());
        payment.setCreatedAt(paymentOrigin.getCreatedAt());
        payment.setStatus(paymentOrigin.getStatus().getSetField().getFieldName());
        payment.setAmount(paymentOrigin.getCost().getAmount());
        payment.setCurrency(paymentOrigin.getCost().getCurrency().getSymbolicCode());
        payment.setPaymentToolToken(paymentOrigin.getPayer().getPaymentTool().getBankCard().getToken());
        payment.setPaymentSession(paymentOrigin.getPayer().getSessionId());
        payment.setContactInfo(new PaymentContactInfo(paymentOrigin.getPayer().getContactInfo().getEmail(), paymentOrigin.getPayer().getContactInfo().getPhoneNumber()));
        payment.setIp(paymentOrigin.getPayer().getClientInfo().getIpAddress());
        payment.setFingerprint(paymentOrigin.getPayer().getClientInfo().getFingerprint());
    }

    @Override
    protected Message getMessage(String invoiceId) {
        return messageDao.getAny(invoiceId, INVOICE);
    }
}
