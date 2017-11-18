package com.rbkmoney.hooker.dao;

import java.util.Collection;
import java.util.List;

/**
 * Created by inalarsanukaev on 14.11.17.
 */
public interface QueueDao<Q> {
    void createWithPolicy(long messageId) throws DaoException;
    List<Q> getWithPolicies(Collection<Long> ids);
    void delete(long id);
}
