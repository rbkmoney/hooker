package com.rbkmoney.hooker.handler;

import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.hooker.model.EventInfo;
import com.rbkmoney.hooker.model.Message;

/**
 * Created by inal on 24.11.2016.
 */
public interface Handler<C, M extends Message> {
    default boolean accept(C change) {
        return getFilter().match(change);
    }

    void handle(C change, EventInfo eventInfo);

    Filter getFilter();
}
