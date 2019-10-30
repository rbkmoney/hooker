package com.rbkmoney.hooker.service;

import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.swag_webhook_events.model.*;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CustomerEventService implements EventService<CustomerMessage> {

    @Override
    public Event getByMessage(CustomerMessage message) {
        Event event = resolveEvent(message);
        event.setEventID(message.getEventId().intValue());
        event.setOccuredAt(OffsetDateTime.parse(message.getOccuredAt(), DateTimeFormatter.ISO_DATE_TIME));
        event.setTopic(Event.TopicEnum.CUSTOMERSTOPIC);
        return event;
    }

    private Event resolveEvent(CustomerMessage message) {
        switch (message.getEventType()) {
            case CUSTOMER_CREATED: return new CustomerCreated().customer(message.getCustomer());
            case CUSTOMER_DELETED: return new CustomerDeleted().customer(message.getCustomer());
            case CUSTOMER_READY: return new CustomerReady().customer(message.getCustomer());
            case CUSTOMER_BINDING_STARTED: return new CustomerBindingStarted().customer(message.getCustomer()).binding(message.getCustomerBinding());
            case CUSTOMER_BINDING_SUCCEEDED: return new CustomerBindingSucceeded().customer(message.getCustomer()).binding(message.getCustomerBinding());
            case CUSTOMER_BINDING_FAILED: return new CustomerBindingFailed().customer(message.getCustomer()).binding(message.getCustomerBinding());
            default: throw new UnsupportedOperationException("Unknown event type " + message.getEventType());
        }
    }
}
