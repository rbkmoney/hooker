package com.rbkmoney.hooker.handler;

import com.rbkmoney.geck.filter.Filter;

/**
 * Created by inal on 24.11.2016.
 */
public interface Handler<C> {
    default boolean accept(C change) {
        return getFilter().match(change);
    }
    void handle(C change, Long eventId, String eventCreatedAt, String sourceId, Long sequenceId, Integer changeId);
    Filter getFilter();
}
