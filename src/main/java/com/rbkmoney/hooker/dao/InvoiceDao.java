package com.rbkmoney.hooker.dao;

public interface InvoiceDao {
    InvoiceInfo get(String invoiceId) throws DaoException;
    boolean add(InvoiceInfo invoiceInfo) throws DaoException;

    Long getMaxEventId();

    boolean delete(String invoiceId) throws DaoException;
}
