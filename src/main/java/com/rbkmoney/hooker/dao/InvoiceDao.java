package com.rbkmoney.hooker.dao;

import com.rbkmoney.damsel.domain.Invoice;

public interface InvoiceDao {
    InvoiceInfo get(String invoiceId) throws Exception;
    boolean add(InvoiceInfo invoiceInfo) throws Exception;
    boolean delete(String invoiceId) throws Exception;
}
