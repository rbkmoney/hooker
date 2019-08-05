package com.rbkmoney.hooker.handler;

import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.hooker.model.Message;

/**
 * Created by inal on 24.11.2016.
 */
public interface Handler<C, M extends Message> {
    default boolean accept(C change) {
        return getFilter().match(change);
    }
    M handle(C change, Long eventId, String eventCreatedAt, String sourceId, Long sequenceId, Integer changeId);
    Filter getFilter();
}
