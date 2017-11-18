package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.domain.Invoice;
import com.rbkmoney.damsel.domain.InvoiceCart;
import com.rbkmoney.damsel.domain.InvoiceLine;
import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.QueueDao;
import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.dao.impl.InvoicingTaskDao;
import com.rbkmoney.hooker.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Component
public class InvoiceCreatedHandler extends AbstractInvoiceEventHandler {

    @Autowired
    MessageDao messageDao;

    @Autowired
    QueueDao queueDao;

    @Autowired
    InvoicingTaskDao taskDao;

    private Filter filter;

    private EventType eventType = EventType.INVOICE_CREATED;

    public InvoiceCreatedHandler() {
        filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule(), new IsNullCondition().not()));
    }

    @Override
    @Transactional
    protected void saveEvent(InvoiceChange ic, Event event) throws DaoException {
        Invoice invoiceOrigin = ic.getInvoiceCreated().getInvoice();
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
        InvoiceCart cart = invoiceOrigin.getDetails().getCart();
        if (cart != null && !cart.getLines().isEmpty()) {
            invoice.setCart(new ArrayList<>());
            for (InvoiceLine l : cart.getLines()) {
                InvoiceCartPosition icp = new InvoiceCartPosition();
                icp.setProduct(l.getProduct());
                icp.setPrice(l.getPrice().getAmount());
                icp.setQuantity(l.getQuantity());
                icp.setCost(l.getPrice().getAmount() * l.getQuantity());
                if (l.getMetadata() != null) {
                    Value v = l.getMetadata().get("TaxMode");
                    if (v != null) {
                        icp.setTaxMode(new TaxMode(v.getStr()));
                    }
                }
                invoice.getCart().add(icp);
            }
        }
        messageDao.create(message);
        queueDao.createWithPolicy(message.getId());
        // create tasks
        taskDao.create(message.getId());
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
