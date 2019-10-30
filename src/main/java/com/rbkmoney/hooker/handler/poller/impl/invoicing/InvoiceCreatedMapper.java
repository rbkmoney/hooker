package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.domain.Invoice;
import com.rbkmoney.damsel.domain.InvoiceCart;
import com.rbkmoney.damsel.domain.InvoiceLine;
import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.model.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Map;

@Component
public class InvoiceCreatedMapper extends AbstractInvoiceEventMapper {

    private EventType eventType = EventType.INVOICE_CREATED;

    private Filter filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule(), new IsNullCondition().not()));

    @Override
    @Transactional
    public InvoicingMessage buildEvent(InvoiceChange ic, EventInfo eventInfo, Map<InvoicingMessageKey, InvoicingMessage> storage) throws DaoException {
        Invoice invoiceOrigin = ic.getInvoiceCreated().getInvoice();
        InvoicingMessage message = new InvoicingMessage();
        message.setEventTime(eventInfo.getEventCreatedAt());
        message.setSequenceId(eventInfo.getSequenceId());
        message.setChangeId(eventInfo.getChangeId());
        message.setType(InvoicingMessageEnum.INVOICE.value());
        message.setPartyId(invoiceOrigin.getOwnerId());
        message.setEventType(eventType);
        message.setInvoiceId(invoiceOrigin.getId());
        message.setShopID(invoiceOrigin.getShopId());
        message.setInvoiceStatus(InvoiceStatusEnum.valueOf(invoiceOrigin.getStatus().getSetField().getFieldName()));
        return message;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
