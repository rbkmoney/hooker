package com.rbkmoney.hooker.dao;

import java.util.Collection;
import java.util.List;

public interface MessageDao<M> {
    void createEvent(M message) throws DaoException;
    void createData(M message) throws DaoException;
    Long getMaxEventId();
    List<M> getBy(Collection<Long> messageIds) throws DaoException;
}
