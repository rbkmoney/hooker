package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.domain.FinalCashFlowPosting;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefundCreated;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.Payment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvoicePaymentRefundStartedHandler extends NeedReadInvoiceEventHandler {

    private Filter filter;
    private EventType eventType = EventType.INVOICE_PAYMENT_REFUND_PROCESSED;

    public InvoicePaymentRefundStartedHandler() {
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
        return EventType.INVOICE_PAYMENT_STATUS_CHANGED;
    }

    @Override
    protected void modifyMessage(InvoiceChange ic, Event event, InvoicingMessage message) {
        InvoicePaymentRefundCreated refundCreated = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getPayload().getInvoicePaymentRefundCreated();
        Payment payment = message.getPayment();
        payment.setCreatedAt(refundCreated.getRefund().getCreatedAt());
        payment.setStatus("refund_created");
        List<FinalCashFlowPosting> cashFlow = refundCreated.getCashFlow();
        payment.setAmount(getAmount(cashFlow));
        payment.setCurrency(cashFlow.get(0).getVolume().getCurrency().getSymbolicCode());
    }

    public static long getAmount(List<FinalCashFlowPosting> finalCashFlow) {
        return finalCashFlow.stream()
                .filter(c -> c.getSource().getAccountType().isSetMerchant() &&
                        c.getDestination().getAccountType().isSetProvider())
                .mapToLong(c -> c.getVolume().getAmount())
                .sum();
    }
}
