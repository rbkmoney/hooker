package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.domain.FinalCashFlowPosting;
import com.rbkmoney.damsel.domain.InvoicePaymentRefund;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefundCreated;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.Refund;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvoicePaymentRefundStartedHandler extends NeedReadInvoiceEventHandler {

    private Filter filter;
    private EventType eventType = EventType.INVOICE_PAYMENT_REFUND_STARTED;

    public InvoicePaymentRefundStartedHandler() {
        filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule(), new IsNullCondition().not()));
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    protected InvoicingMessage getMessage(String invoiceId, InvoiceChange ic) {
        String paymentId = ic.getInvoicePaymentChange().getId();
        return messageDao.getPayment(invoiceId, paymentId);
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
        InvoicePaymentRefundCreated refundCreated = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getPayload().getInvoicePaymentRefundCreated();
        Refund refund = new Refund();
        message.setRefund(refund);
        InvoicePaymentRefund refundOrigin = refundCreated.getRefund();
        refund.setId(refundOrigin.getId());
        refund.setCreatedAt(refundOrigin.getCreatedAt());
        refund.setStatus(refundOrigin.getStatus().getSetField().getFieldName());
        List<FinalCashFlowPosting> cashFlow = refundCreated.getCashFlow();
        if (refundOrigin.isSetCash()) {
            refund.setAmount(refundOrigin.getCash().getAmount());
            refund.setCurrency(refundOrigin.getCash().getCurrency().getSymbolicCode());
        } else {
            refund.setAmount(getAmount(cashFlow));
            refund.setCurrency(cashFlow.get(0).getVolume().getCurrency().getSymbolicCode());
        }
        refund.setReason(refundOrigin.getReason());
    }

    public static long getAmount(List<FinalCashFlowPosting> finalCashFlow) {
        return finalCashFlow.stream()
                .filter(c -> c.getSource().getAccountType().isSetMerchant() &&
                        c.getDestination().getAccountType().isSetProvider())
                .mapToLong(c -> c.getVolume().getAmount())
                .sum();
    }
}
