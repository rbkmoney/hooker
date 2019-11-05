package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.damsel.payment_processing.Customer;
import com.rbkmoney.hooker.converter.CustomerBindingConverter;
import com.rbkmoney.hooker.converter.CustomerConverter;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.swag_webhook_events.model.CustomerBindingFailed;
import com.rbkmoney.swag_webhook_events.model.CustomerBindingStarted;
import com.rbkmoney.swag_webhook_events.model.CustomerBindingSucceeded;
import com.rbkmoney.swag_webhook_events.model.CustomerCreated;
import com.rbkmoney.swag_webhook_events.model.CustomerDeleted;
import com.rbkmoney.swag_webhook_events.model.CustomerReady;
import com.rbkmoney.swag_webhook_events.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CustomerEventService implements EventService<CustomerMessage> {

    private final CustomerManagementSrv.Iface customerClient;
    private final CustomerConverter customerConverter;
    private final CustomerBindingConverter customerBindingConverter;

    @SneakyThrows
    @Override
    public Event getByMessage(CustomerMessage message) {
        Customer customer = customerClient.get(message.getCustomerId(), new EventRange().setLimit(message.getSequenceId().intValue()));
        return resolveEvent(message, customer)
                .eventID(message.getEventId().intValue())
                .occuredAt(OffsetDateTime.parse(message.getEventTime(), DateTimeFormatter.ISO_DATE_TIME))
                .topic(Event.TopicEnum.CUSTOMERSTOPIC);
    }

    private Event resolveEvent(CustomerMessage message, Customer customer) {
        switch (message.getEventType()) {
            case CUSTOMER_CREATED: return new CustomerCreated().customer(customerConverter.convert(customer));
            case CUSTOMER_DELETED: return new CustomerDeleted().customer(customerConverter.convert(customer));
            case CUSTOMER_READY: return new CustomerReady().customer(customerConverter.convert(customer));
            case CUSTOMER_BINDING_STARTED: return new CustomerBindingStarted()
                    .customer(customerConverter.convert(customer))
                    .binding(customerBindingConverter.convert(extractBinding(message, customer)));
            case CUSTOMER_BINDING_SUCCEEDED: return new CustomerBindingSucceeded()
                    .customer(customerConverter.convert(customer))
                    .binding(customerBindingConverter.convert(extractBinding(message, customer)));
            case CUSTOMER_BINDING_FAILED: return new CustomerBindingFailed()
                    .customer(customerConverter.convert(customer))
                    .binding(customerBindingConverter.convert(extractBinding(message, customer)));
            default: throw new UnsupportedOperationException("Unknown event type " + message.getEventType());
        }
    }

    private com.rbkmoney.damsel.payment_processing.CustomerBinding extractBinding(CustomerMessage message, Customer customer){
        return customer.getBindings().stream()
                .filter(b -> b.getId().equals(message.getBindingId()))
                .findFirst()
                .orElseThrow();
    }
}
