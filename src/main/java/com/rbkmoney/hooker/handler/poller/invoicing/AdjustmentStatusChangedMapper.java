package com.rbkmoney.hooker.handler.poller.invoicing;

import com.rbkmoney.damsel.payment_processing.Invoice;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicingSrv;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.exception.RemoteHostException;
import com.rbkmoney.hooker.model.*;
import com.rbkmoney.hooker.utils.HellgateUtils;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

@Component
public class AdjustmentStatusChangedMapper extends NeedReadInvoiceEventMapper {

    private final InvoicingSrv.Iface invoicingClient;

    private static final EventType EVENT_TYPE = EventType.INVOICE_PAYMENT_STATUS_CHANGED;

    private static final String ADJUSTMENT_STATUS_CHANGED_PATH = "invoice_payment_change.payload." +
            "invoice_payment_adjustment_change.payload.invoice_payment_adjustment_status_changed." +
            "status.captured";

    private static final Filter FILTER = new PathConditionFilter(
            new PathConditionRule(ADJUSTMENT_STATUS_CHANGED_PATH, new IsNullCondition().not())
    );
    public AdjustmentStatusChangedMapper(InvoicingMessageDao messageDao,
                                         InvoicingSrv.Iface invoicingClient) {
        super(messageDao);
        this.invoicingClient = invoicingClient;
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
        try {
            Invoice invoiceInfo = invoicingClient.get(
                    HellgateUtils.USER_INFO,
                    message.getInvoiceId(),
                    HellgateUtils.getEventRange(message.getSequenceId().intValue())
            );
            invoiceInfo.getPayments().stream()
                    .filter(payment -> message.getPaymentId().equalsIgnoreCase(payment.getPayment().getId()))
                    .findFirst()
                    .map(payment -> payment.getPayment().getStatus())
                    .ifPresent(
                            paymentStatus -> message.setPaymentStatus(
                                    PaymentStatusEnum.lookup(paymentStatus.getSetField().getFieldName())
                            )
                    );
        } catch (TException e) {
            throw new RemoteHostException(e);
        }
    }
}
