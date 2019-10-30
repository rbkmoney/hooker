package com.rbkmoney.hooker.service;

import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.swag_webhook_events.model.Event;

public interface EventService<M extends Message> {
    Event getByMessage(M message);
}
