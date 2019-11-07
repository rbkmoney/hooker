package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.exception.DaoException;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.exception.NotFoundException;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.InvoicingMessageEnum;
import com.rbkmoney.hooker.model.InvoicingMessageKey;
import com.rbkmoney.hooker.utils.CashFlowUtils;
import org.springframework.stereotype.Component;

@Component
public class InvoicePaymentCashFlowChangedMapper extends NeedReadInvoiceEventMapper {

    private final EventType eventType = EventType.INVOICE_PAYMENT_CASH_FLOW_CHANGED;

    private final Filter filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftPath(), new IsNullCondition().not()));

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
        message.setPaymentFee(CashFlowUtils.getFeeAmount(ic.getInvoicePaymentChange().getPayload().getInvoicePaymentCashFlowChanged().getCashFlow()));
    }

    @Override
    public Filter getFilter() {
        return filter;
    }
}
