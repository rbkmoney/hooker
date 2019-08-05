package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.domain.Cash;
import com.rbkmoney.damsel.domain.InvoicePaymentCaptured;
import com.rbkmoney.damsel.domain.InvoicePaymentStatus;
import com.rbkmoney.damsel.domain.OperationFailure;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.Payment;
import com.rbkmoney.hooker.utils.ErrorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvoicePaymentStatusChangedHandler extends NeedReadInvoiceEventHandler {

    private EventType eventType = EventType.INVOICE_PAYMENT_STATUS_CHANGED;

    private Filter filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule(), new IsNullCondition().not()));

    private final InvoicingMessageDao messageDao;

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    protected InvoicingMessage getMessage(String invoiceId, InvoiceChange ic) {
        return messageDao.getPayment(invoiceId, ic.getInvoicePaymentChange().getId());
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
    protected void modifyMessage(InvoiceChange ic, InvoicingMessage message) {
        InvoicePaymentStatus paymentOriginStatus = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentStatusChanged().getStatus();
        Payment payment = message.getPayment();
        payment.setStatus(paymentOriginStatus.getSetField().getFieldName());
        if (paymentOriginStatus.isSetFailed()) {
            OperationFailure failure = paymentOriginStatus.getFailed().getFailure();
            payment.setError(ErrorUtils.getPaymentError(failure));
        } else if (paymentOriginStatus.isSetCaptured()) {
            InvoicePaymentCaptured invoicePaymentCaptured = paymentOriginStatus.getCaptured();
            if (invoicePaymentCaptured.isSetCost()) {
                Cash cost = invoicePaymentCaptured.getCost();
                payment.setAmount(cost.getAmount());
                payment.setCurrency(cost.getCurrency().getSymbolicCode());
            }
        }
    }
}
