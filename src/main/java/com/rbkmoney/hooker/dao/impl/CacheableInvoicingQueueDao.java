package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.CacheMng;
import com.rbkmoney.hooker.model.InvoicingQueue;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CacheableInvoicingQueueDao extends InvoicingQueueDao {
    @Autowired
    CacheMng cacheMng;

    public CacheableInvoicingQueueDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<InvoicingQueue> getWithPolicies(Collection<Long> ids) {
        List<InvoicingQueue> queues = cacheMng.getQueues(ids, InvoicingQueue.class);
        if (queues.size() == ids.size()) {
            return queues;
        }
        Set<Long> cacheIds = new HashSet<>(ids);
        queues.forEach(h -> cacheIds.remove(h.getId()));
        List<InvoicingQueue> queuesFromDb = super.getWithPolicies(cacheIds);
        cacheMng.putQueues(queuesFromDb);
        queues.addAll(queuesFromDb);
        return queues;
    }
}
