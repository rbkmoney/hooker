package com.rbkmoney.hooker.handler;

import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.hooker.model.EventInfo;
import com.rbkmoney.hooker.model.InvoicingMessageKey;
import com.rbkmoney.hooker.model.Message;

import java.util.Map;

/**
 * Created by inal on 24.11.2016.
 */
public interface Mapper<C, M extends Message> {
    default boolean accept(C change) {
        return getFilter().match(change);
    }
    M handle(C change, EventInfo eventInfo, Map<InvoicingMessageKey, M> storage);
    Filter getFilter();
}
