package com.rbkmoney.hooker.dao;

import java.util.Collection;
import java.util.List;

public interface MessageDao<M> {
    List<M> getBy(Collection<Long> messageIds) throws DaoException;
}
