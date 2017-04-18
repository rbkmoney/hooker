package com.rbkmoney.hooker.dao;


import com.rbkmoney.hooker.model.Task;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by jeckep on 13.04.17.
 */
public interface TaskDao {
    void create(Collection<Long> messageIds);
    void remove(long hookId, long messageId);
    List<Task> getAll();
    Map<Long, List<Task>> getScheduled(Collection<Long> excludeHooksIds);
}
