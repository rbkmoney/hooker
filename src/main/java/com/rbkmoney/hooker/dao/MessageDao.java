package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.model.Message;

public interface MessageDao {
    Message getAny(String invoiceId) throws DaoException;
    boolean save(Message message) throws DaoException;
    boolean delete(String invoiceId) throws DaoException;
    boolean delete(long id) throws DaoException;

    Long getMaxEventId();
}
