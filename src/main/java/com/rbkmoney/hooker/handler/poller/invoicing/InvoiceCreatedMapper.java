package com.rbkmoney.hooker.handler.poller.invoicing;

import com.rbkmoney.damsel.domain.Invoice;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.exception.DaoException;
import com.rbkmoney.hooker.model.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
public class InvoiceCreatedMapper extends AbstractInvoiceEventMapper {

    private EventType eventType = EventType.INVOICE_CREATED;

    private Filter filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftPath(), new IsNullCondition().not()));

    @Override
    @Transactional
    public InvoicingMessage buildEvent(InvoiceChange ic, EventInfo eventInfo, Map<InvoicingMessageKey, InvoicingMessage> storage) throws DaoException {
        Invoice invoiceOrigin = ic.getInvoiceCreated().getInvoice();
        InvoicingMessage message = new InvoicingMessage();
        message.setEventTime(eventInfo.getEventCreatedAt());
        message.setSequenceId(eventInfo.getSequenceId());
        message.setChangeId(eventInfo.getChangeId());
        message.setType(InvoicingMessageEnum.INVOICE);
        message.setPartyId(invoiceOrigin.getOwnerId());
        message.setEventType(eventType);
        message.setInvoiceId(invoiceOrigin.getId());
        message.setShopId(invoiceOrigin.getShopId());
        message.setInvoiceStatus(InvoiceStatusEnum.lookup(invoiceOrigin.getStatus().getSetField().getFieldName()));
        return message;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
