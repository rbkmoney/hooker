package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.model.*;
import org.springframework.stereotype.Component;

@Component
public class InvoiceStatusChangedMapper extends NeedReadInvoiceEventMapper {

    private EventType eventType = EventType.INVOICE_STATUS_CHANGED;

    private Filter filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftPath(), new IsNullCondition().not()));

    public InvoiceStatusChangedMapper(InvoicingMessageDao messageDao) {
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
                .type(InvoicingMessageEnum.INVOICE)
                .build();
    }

    @Override
    protected InvoicingMessageEnum getMessageType() {
        return InvoicingMessageEnum.INVOICE;
    }

    @Override
    protected EventType getEventType() {
        return eventType;
    }

    @Override
    protected void modifyMessage(InvoiceChange ic, InvoicingMessage message) {
        message.setInvoiceStatus(InvoiceStatusEnum.lookup(ic.getInvoiceStatusChanged().getStatus().getSetField().getFieldName()));
    }
}
