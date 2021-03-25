package com.rbkmoney.hooker.handler.poller.invoicing;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.InvoicingMessageEnum;
import com.rbkmoney.hooker.model.InvoicingMessageKey;
import com.rbkmoney.hooker.model.RefundStatusEnum;
import org.springframework.stereotype.Component;

@Component
public class InvoicePaymentRefundStatusChangedMapper extends NeedReadInvoiceEventMapper {

    private EventType eventType = EventType.INVOICE_PAYMENT_REFUND_STATUS_CHANGED;

    private Filter filter =
            new PathConditionFilter(new PathConditionRule(eventType.getThriftPath(), new IsNullCondition().not()));

    public InvoicePaymentRefundStatusChangedMapper(InvoicingMessageDao messageDao) {
        super(messageDao);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    protected InvoicingMessageKey getMessageKey(String invoiceId, InvoiceChange ic) {
        return InvoicingMessageKey.builder()
                .invoiceId(invoiceId)
                .paymentId(ic.getInvoicePaymentChange().getId())
                .refundId(ic.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getId())
                .type(InvoicingMessageEnum.REFUND)
                .build();
    }

    @Override
    protected InvoicingMessageEnum getMessageType() {
        return InvoicingMessageEnum.REFUND;
    }

    @Override
    protected EventType getEventType() {
        return eventType;
    }

    @Override
    protected void modifyMessage(InvoiceChange ic, InvoicingMessage message) {
        message.setRefundStatus(
                RefundStatusEnum.lookup(ic.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange()
                        .getPayload().getInvoicePaymentRefundStatusChanged().getStatus().getSetField().getFieldName()));
    }
}
