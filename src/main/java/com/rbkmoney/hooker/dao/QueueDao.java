package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.model.Queue;

import java.util.Collection;
import java.util.List;

/**
 * Created by inalarsanukaev on 14.11.17.
 */
public interface QueueDao<Q extends Queue> {
    void createWithPolicy(long messageId) throws DaoException;
    List<Q> getWithPolicies(Collection<Long> ids);
    void disable(long id);
}
