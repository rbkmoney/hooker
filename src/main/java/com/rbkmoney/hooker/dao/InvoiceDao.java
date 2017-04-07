package com.rbkmoney.hooker.dao;

public interface InvoiceDao {
    String getParty(String invoiceId) throws Exception;

    boolean add(String partyId, String invoiceId) throws Exception;

    boolean delete(String invoiceId) throws Exception;
}
