package com.rbkmoney.hooker.handler.poller.impl;

import com.rbkmoney.damsel.domain.Invoice;
import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.EventTypeCode;
import com.rbkmoney.hooker.dao.InvoiceDao;
import com.rbkmoney.hooker.dao.InvoiceInfo;
import com.rbkmoney.hooker.handler.PollingException;
import com.rbkmoney.thrift.filter.Filter;
import com.rbkmoney.thrift.filter.PathConditionFilter;
import com.rbkmoney.thrift.filter.rule.PathConditionRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoiceCreatedHandler extends AbstractInvoiceEventHandler {

    @Autowired
    InvoiceDao invoiceDao;

    private Filter filter;

    private EventTypeCode code = EventTypeCode.INVOICE_CREATED;

    public InvoiceCreatedHandler() {
        filter = new PathConditionFilter(new PathConditionRule(code.getKey()));
    }

    @Override
    public void handle(StockEvent value) throws PollingException {
        try {
            addInvoice(value);
        } catch (DaoException e){
            throw new PollingException(e);
        }
        super.handle(value);
    }

    private void addInvoice(StockEvent value) throws DaoException{
        Event event = value.getSourceEvent().getProcessingEvent();
        Invoice invoice = event.getPayload().getInvoiceEvent().getInvoiceCreated().getInvoice();
        InvoiceInfo invoiceInfo = new InvoiceInfo();
        invoiceInfo.setEventId(event.getId());
        invoiceInfo.setInvoiceId(event.getSource().getInvoice());
        invoiceInfo.setPartyId(invoice.getOwnerId());
        invoiceInfo.setShopId(invoice.getShopId());
        invoiceInfo.setAmount(invoice.getCost().getAmount());
        invoiceInfo.setCurrency(invoice.getCost().getCurrency().getSymbolicCode());
        invoiceInfo.setCreatedAt(invoice.getCreatedAt());
        invoiceInfo.setMetadata(invoice.getContext());
        if (!invoiceDao.add(invoiceInfo)) {
            throw new DaoException("Couldn't add invoice with id "+invoiceInfo.getInvoiceId());
        }
    }

    @Override
    protected EventTypeCode getCode() {
        return code;
    }

    @Override
    protected void prepareInvoiceInfo(Event event, InvoiceInfo invoiceInfo) {
        invoiceInfo.setDescription("Создание инвойса");
        invoiceInfo.setStatus(event.getPayload().getInvoiceEvent().getInvoiceCreated().getInvoice().getStatus().getSetField().getFieldName());
        invoiceInfo.setEventType("invoice");
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
