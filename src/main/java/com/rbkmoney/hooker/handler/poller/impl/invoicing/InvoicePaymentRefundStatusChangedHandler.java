package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.domain.InvoicePaymentRefundStatus;
import com.rbkmoney.damsel.domain.OperationFailure;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.Refund;
import com.rbkmoney.hooker.utils.ErrorUtils;
import org.springframework.stereotype.Component;

@Component
public class InvoicePaymentRefundStatusChangedHandler extends NeedReadInvoiceEventHandler {

    private Filter filter;
    private EventType eventType = EventType.INVOICE_PAYMENT_REFUND_STATUS_CHANGED;

    public InvoicePaymentRefundStatusChangedHandler() {
        filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule(), new IsNullCondition().not()));
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    protected InvoicingMessage getMessage(String invoiceId, InvoiceChange ic) {
        String paymentId = ic.getInvoicePaymentChange().getId();
        String refundId = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getId();
        return messageDao.getRefund(invoiceId, paymentId, refundId);
    }

    @Override
    protected String getMessageType() {
        return REFUND;
    }

    @Override
    protected EventType getEventType() {
        return eventType;
    }

    @Override
    protected void modifyMessage(InvoiceChange ic, Event event, InvoicingMessage message) {
        InvoicePaymentRefundStatus refundStatus = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getPayload().getInvoicePaymentRefundStatusChanged().getStatus();
        Refund refund = message.getRefund();
        refund.setStatus(refundStatus.getSetField().getFieldName());
        if (refundStatus.isSetFailed()) {
            OperationFailure failure = refundStatus.getFailed().getFailure();
            refund.setError(ErrorUtils.getPaymentError(failure));
        }
    }
}
