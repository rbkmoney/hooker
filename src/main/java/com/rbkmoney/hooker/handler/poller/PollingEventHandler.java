package com.rbkmoney.hooker.handler.poller;

import com.rbkmoney.hooker.handler.Handler;
import com.rbkmoney.thrift.filter.Filter;

public interface PollingEventHandler<T> extends Handler<T> {
    default boolean accept(T value) {
        return getFilter().match(value);
    }

    Filter getFilter();
}
