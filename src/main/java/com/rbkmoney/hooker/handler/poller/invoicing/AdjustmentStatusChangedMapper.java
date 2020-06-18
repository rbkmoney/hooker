package com.rbkmoney.hooker.handler.poller.invoicing;

import com.rbkmoney.damsel.domain.InvoicePaymentAdjustmentStatus;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.model.*;
import org.springframework.stereotype.Component;

@Component
public class AdjustmentStatusChangedMapper extends NeedReadInvoiceEventMapper {

    private EventType eventType = EventType.INVOICE_PAYMENT_STATUS_CHANGED;

    private static final String ADJUSTMENT_StATUS_CHANGED_PATH = "invoice_payment_change.payload." +
            "invoice_payment_adjustment_change.payload.invoice_payment_adjustment_status_changed";

    private Filter filter = new PathConditionFilter(
            new PathConditionRule(ADJUSTMENT_StATUS_CHANGED_PATH, new IsNullCondition().not())
    );

    public AdjustmentStatusChangedMapper(InvoicingMessageDao messageDao) {
        super(messageDao);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    public boolean accept(InvoiceChange change) {
        if (change.isSetInvoicePaymentChange()
                && change.getInvoicePaymentChange().getPayload().isSetInvoicePaymentAdjustmentChange()
                && change.getInvoicePaymentChange().getPayload().getInvoicePaymentAdjustmentChange().getPayload().isSetInvoicePaymentAdjustmentStatusChanged()) {
            InvoicePaymentAdjustmentStatus status =
                    change.getInvoicePaymentChange().getPayload().getInvoicePaymentAdjustmentChange().getPayload()
                            .getInvoicePaymentAdjustmentStatusChanged().getStatus();
            return status.isSetCaptured() || status.isSetCancelled();
        }
        return false;
    }

    @Override
    protected InvoicingMessageKey getMessageKey(String invoiceId, InvoiceChange ic) {
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
        message.setPaymentStatus(PaymentStatusEnum.lookup(ic.getInvoicePaymentChange().getPayload()
                .getInvoicePaymentStatusChanged().getStatus().getSetField().getFieldName()));
    }
}
