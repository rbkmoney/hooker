package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.domain.Invoice;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.thrift.filter.Filter;
import com.rbkmoney.thrift.filter.PathConditionFilter;
import com.rbkmoney.thrift.filter.rule.PathConditionRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoiceCreatedHandler extends AbstractInvoiceEventHandler {

    @Autowired
    MessageDao messageDao;

    private Filter filter;

    private EventType eventType = EventType.INVOICE_CREATED;

    public InvoiceCreatedHandler() {
        filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule()));
    }

    @Override
    protected void saveEvent(Event event) throws DaoException {
        Invoice invoice = event.getPayload().getInvoiceEvent().getInvoiceCreated().getInvoice();
        Message message = new Message();
        ////static invoice data
        message.setInvoiceId(invoice.getId());
        message.setPartyId(invoice.getOwnerId());
        message.setShopId(invoice.getShopId());
        message.setAmount(invoice.getCost().getAmount());
        message.setCurrency(invoice.getCost().getCurrency().getSymbolicCode());
        message.setCreatedAt(invoice.getCreatedAt());
        message.setProduct(invoice.getDetails().getProduct());
        message.setDescription(invoice.getDetails().getDescription());
        message.setMetadata(invoice.getContext());
        ////dynamic payment data
        message.setStatus(event.getPayload().getInvoiceEvent().getInvoiceCreated().getInvoice().getStatus().getSetField().getFieldName());
        message.setType(INVOICE);
        message.setEventType(eventType);
        message.setEventId(event.getId());

        messageDao.create(message);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
