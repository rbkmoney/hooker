package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.model.InvoicingQueue;

import java.util.Collection;
import java.util.List;

/**
 * Created by inalarsanukaev on 14.11.17.
 */
public interface InvoicingQueueDao {
    void createWithPolicy(long messageId) throws DaoException;
    List<InvoicingQueue> getWithPolicies(Collection<Long> ids);
}
