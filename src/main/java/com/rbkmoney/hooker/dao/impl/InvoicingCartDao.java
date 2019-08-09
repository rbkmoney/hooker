package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.model.InvoiceCartPosition;

import java.util.List;

public interface InvoicingCartDao {
    List<InvoiceCartPosition> getByMessageId(Long messageId) throws DaoException;
    int[] saveBatch(List<InvoiceCartPosition> carts) throws DaoException;
}
