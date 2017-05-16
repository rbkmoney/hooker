package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.domain.Invoice;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.InvoiceContent;
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
        Invoice invoiceOrigin = event.getPayload().getInvoiceEvent().getInvoiceCreated().getInvoice();
        //////
        Message message = new Message();
        message.setEventId(event.getId());
        message.setEventTime(event.getCreatedAt());
        message.setType(INVOICE);
        message.setPartyId(invoiceOrigin.getOwnerId());
        message.setEventType(eventType);
        com.rbkmoney.hooker.model.Invoice invoice = new com.rbkmoney.hooker.model.Invoice();
        message.setInvoice(invoice);
        invoice.setId(invoiceOrigin.getId());
        invoice.setShopID(invoiceOrigin.getShopId());
        invoice.setCreatedAt(invoiceOrigin.getCreatedAt());
        invoice.setStatus(invoiceOrigin.getStatus().getSetField().getFieldName());
        invoice.setDueDate(invoiceOrigin.getDue());
        invoice.setAmount(invoiceOrigin.getCost().getAmount());
        invoice.setCurrency(invoiceOrigin.getCost().getCurrency().getSymbolicCode());
        InvoiceContent metadata = new InvoiceContent();
        metadata.setType(invoiceOrigin.getContext().getType());
        metadata.setData(invoiceOrigin.getContext().getData());
        invoice.setMetadata(metadata);
        invoice.setProduct(invoiceOrigin.getDetails().getProduct());
        invoice.setDescription(invoiceOrigin.getDetails().getDescription());
        messageDao.create(message);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
