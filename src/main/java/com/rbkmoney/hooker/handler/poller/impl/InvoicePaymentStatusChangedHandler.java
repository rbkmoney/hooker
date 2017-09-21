package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.domain.ExternalFailure;
import com.rbkmoney.damsel.domain.InvoicePaymentStatus;
import com.rbkmoney.damsel.domain.OperationFailure;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.model.Payment;
import com.rbkmoney.hooker.model.PaymentStatusError;
import org.springframework.stereotype.Component;

@Component
public class InvoicePaymentStatusChangedHandler extends NeedReadInvoiceEventHandler {

    private Filter filter;
    private EventType eventType = EventType.INVOICE_PAYMENT_STATUS_CHANGED;

    public InvoicePaymentStatusChangedHandler() {
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
        InvoicePaymentStatus paymentOriginStatus = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentStatusChanged().getStatus();
        Payment payment = message.getPayment();
        payment.setStatus(paymentOriginStatus.getSetField().getFieldName());
        if (paymentOriginStatus.isSetFailed()) {
            OperationFailure failure = paymentOriginStatus.getFailed().getFailure();
            if (failure.isSetExternalFailure()) {
                ExternalFailure external = failure.getExternalFailure();
                payment.setError(new PaymentStatusError(external.getCode(), external.getDescription()));
            } else if (failure.isSetOperationTimeout()) {
                payment.setError(new PaymentStatusError("408", "Operation timeout"));
            }
        }
    }
}
