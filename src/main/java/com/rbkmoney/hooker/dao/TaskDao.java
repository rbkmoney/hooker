package com.rbkmoney.hooker.dao;


import com.rbkmoney.hooker.exception.DaoException;
import com.rbkmoney.hooker.model.Task;

import java.util.List;
import java.util.Map;

/**
 * Created by jeckep on 13.04.17.
 */
public interface TaskDao {
    void remove(long queueId, long messageId);

    void removeAll(long queueId) throws DaoException;

    Map<Long, List<Task>> getScheduled(int limit) throws DaoException;
}
