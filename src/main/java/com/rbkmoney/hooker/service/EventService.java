package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.payment_processing.EventRange;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.swag_webhook_events.model.Event;

public interface EventService<M extends Message> {
    default EventRange getEventRange(Integer limit) {
        return new EventRange().setLimit(limit);
    }
    Event getByMessage(M message);
}
