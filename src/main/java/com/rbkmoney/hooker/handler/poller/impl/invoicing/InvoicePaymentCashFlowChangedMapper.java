package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.domain.FinalCashFlowPosting;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.dao.NotFoundException;
import com.rbkmoney.hooker.model.*;
import com.rbkmoney.hooker.utils.PaymentToolUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvoicePaymentCashFlowChangedMapper extends NeedReadInvoiceEventMapper {

    private final EventType eventType = EventType.INVOICE_PAYMENT_CASH_FLOW_CHANGED;

    private final Filter filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule(), new IsNullCondition().not()));

    public InvoicePaymentCashFlowChangedMapper(InvoicingMessageDao messageDao) {
        super(messageDao);
    }

    @Override
    protected InvoicingMessageKey getMessageKey(String invoiceId, InvoiceChange ic) throws NotFoundException, DaoException {
        return InvoicingMessageKey.builder()
                .invoiceId(invoiceId)
                .paymentId(ic.getInvoicePaymentChange().getId())
                .type(InvoicingMessageEnum.PAYMENT)
                .build();
    }

    @Override
    protected InvoicingMessageEnum getMessageType() {
        return InvoicingMessageEnum.PAYMENT;
    }

    @Override
    protected EventType getEventType() {
        return eventType;
    }

    @Override
    protected void modifyMessage(InvoiceChange ic, InvoicingMessage message) {
        Payment payment = message.getPayment();
        List<FinalCashFlowPosting> cashFlow = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentCashFlowChanged().getCashFlow();
        Long feeAmount = PaymentToolUtils.getFeeAmount(cashFlow);
        payment.setFee(feeAmount);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }
}
