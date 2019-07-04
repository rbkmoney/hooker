package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.CacheMng;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.model.InvoicingMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CacheableInvoicingMessageDaoImpl extends InvoicingMessageDaoImpl {

    @Autowired
    CacheMng cacheMng;

    public CacheableInvoicingMessageDaoImpl(DataSource dataSource) {
        super(dataSource);
    }
    @Override
    public void create(InvoicingMessage message) throws DaoException {
        super.create(message);
        putToCache(message);
    }

    @Override
    public InvoicingMessage getInvoice(String invoiceId) throws DaoException {
        InvoicingMessage message = cacheMng.getMessage(invoiceId, InvoicingMessage.class);
        if (message != null) {
            return message.copy();
        }
        InvoicingMessage result = super.getInvoice(invoiceId);
        putToCache(result);
        return result;
    }

    @Override
    public InvoicingMessage getPayment(String invoiceId, String paymentId) throws DaoException {
        InvoicingMessage message = cacheMng.getMessage(invoiceId + "_" + paymentId, InvoicingMessage.class);
        if (message != null) {
            return message.copy();
        }
        InvoicingMessage result = super.getPayment(invoiceId, paymentId);
        putToCache(result);
        return result;
    }

    @Override
    public InvoicingMessage getRefund(String invoiceId, String paymentId, String refundId) throws DaoException {
        InvoicingMessage message = cacheMng.getMessage(invoiceId + "_" + paymentId + "_" + refundId, InvoicingMessage.class);
        if (message != null) {
            return message.copy();
        }
        InvoicingMessage result = super.getRefund(invoiceId, paymentId, refundId);
        putToCache(result);
        return result;
    }

    @Override
    public List<InvoicingMessage> getBy(Collection<Long> ids) throws DaoException {
        List<InvoicingMessage> messages = cacheMng.getMessages(ids, InvoicingMessage.class);
        if (messages.size() == ids.size()) {
            return messages;
        }
        Set<Long> cacheIds = new HashSet<>(ids);
        messages.forEach(m -> cacheIds.remove(m.getId()));
        List<InvoicingMessage> messagesFromDb = super.getBy(cacheIds);
        messagesFromDb.forEach(this::putToCache);
        messages.addAll(messagesFromDb);
        return messages;
    }

    private void putToCache(InvoicingMessage message){
        if (message != null && message.getId() != null) {
            cacheMng.putMessage(message);
            cacheMng.putMessage(message.getInvoice().getId() + (message.isPayment() ? "_" + message.getPayment().getId() : "") + (message.isRefund() ? "_" + message.getPayment().getId() + "_" + message.getRefund().getId() : ""), message);
        }
    }
}
