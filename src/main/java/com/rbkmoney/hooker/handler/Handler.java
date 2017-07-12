package com.rbkmoney.hooker.handler;

import com.rbkmoney.geck.filter.Filter;

/**
 * Created by inal on 24.11.2016.
 */
public interface Handler<C, P> {
    default boolean accept(C change) {
        return getFilter().match(change);
    }
    void handle(C change, P parent);
    Filter getFilter();
}
