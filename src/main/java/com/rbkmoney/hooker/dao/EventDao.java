package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.model.EventStatus;
import com.rbkmoney.hooker.model.InvoiceFatEvent;

import java.util.List;

/**
 * Created by jeckep on 12.04.17.
 */
public interface EventDao {
    List<InvoiceFatEvent> getByStatus(EventStatus status, int limit);
    List<InvoiceFatEvent> getByStatus(EventStatus status);
}
