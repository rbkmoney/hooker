package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.model.Event;
import com.rbkmoney.hooker.model.EventStatus;

import java.util.List;

/**
 * Created by jeckep on 12.04.17.
 */
public interface EventDao {
    List<Event> getByStatus(EventStatus status, int limit);
}
