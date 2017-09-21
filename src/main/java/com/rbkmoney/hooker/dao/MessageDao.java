package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.model.Message;

import java.util.Collection;
import java.util.List;

public interface MessageDao {
    Message getAny(String invoiceId, String type) throws DaoException;
    Message create(Message message) throws DaoException;
    void delete(long id) throws DaoException;
    Long getMaxEventId();
    List<Message> getBy(Collection<Long> messageIds);
}
