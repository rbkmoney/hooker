package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.domain.InvoicePayment;
import com.rbkmoney.damsel.domain.PaymentResourcePayer;
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
import com.rbkmoney.swag_webhook_events.ClientInfo;
import com.rbkmoney.swag_webhook_events.InvoiceCartLine;
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
        if (paymentOrigin.getPayer().isSetPaymentResource()) {
            PaymentResourcePayer payer = paymentOrigin.getPayer().getPaymentResource();
            payment.setPaymentToolToken(payer.getResource().getPaymentTool().getBankCard().getToken());
            payment.setPaymentSession(payer.getResource().getPaymentSessionId());
            payment.setContactInfo(new PaymentContactInfo(payer.getContactInfo().getEmail(), payer.getContactInfo().getPhoneNumber()));
            payment.setIp(payer.getResource().getClientInfo().getIpAddress());
            payment.setFingerprint(payer.getResource().getClientInfo().getFingerprint());
        } else if (paymentOrigin.getPayer().isSetPaymentResource()) {
            //TODO
        }
    }

    @Override
    protected Message getMessage(String invoiceId) {
        return messageDao.getAny(invoiceId, INVOICE);
    }
}
