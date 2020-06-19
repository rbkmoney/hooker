package com.rbkmoney.hooker.handler.poller.invoicing;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.model.*;
import com.rbkmoney.hooker.service.HellgateInvoicingService;
import org.springframework.stereotype.Component;

@Component
public class AdjustmentStatusChangedMapper extends NeedReadInvoiceEventMapper {

    private final HellgateInvoicingService<InvoicingMessage> invoicingEventService;

    private static final EventType EVENT_TYPE = EventType.INVOICE_PAYMENT_STATUS_CHANGED;

    private static final String ADJUSTMENT_STATUS_CHANGED_PATH = "invoice_payment_change.payload." +
            "invoice_payment_adjustment_change.payload.invoice_payment_adjustment_status_changed." +
            "status.captured";

    private static final Filter FILTER = new PathConditionFilter(
            new PathConditionRule(ADJUSTMENT_STATUS_CHANGED_PATH, new IsNullCondition().not())
    );

    public AdjustmentStatusChangedMapper(InvoicingMessageDao messageDao,
                                         HellgateInvoicingService<InvoicingMessage> invoicingEventService) {
        super(messageDao);
        this.invoicingEventService = invoicingEventService;
    }

    @Override
    public Filter getFilter() {
        return FILTER;
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
        return EVENT_TYPE;
    }

    @Override
    protected void modifyMessage(InvoiceChange ic, InvoicingMessage message) {
        InvoicePayment invoicePayment = invoicingEventService.getPaymentByMessage(message);
        message.setPaymentStatus(
                PaymentStatusEnum.lookup(invoicePayment.getPayment().getStatus().getSetField().getFieldName())
        );
    }
}
