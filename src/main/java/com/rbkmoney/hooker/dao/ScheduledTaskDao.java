package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.model.ScheduledTask;

import java.util.Collection;

/**
 * Created by jeckep on 13.04.17.
 */
public interface ScheduledTaskDao {
    void save(Collection<ScheduledTask> task);
    void remove(ScheduledTask task);
}
