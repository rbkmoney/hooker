package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.payment_processing.Customer;
import com.rbkmoney.damsel.payment_processing.CustomerManagementSrv;
import com.rbkmoney.damsel.payment_processing.CustomerNotFound;
import com.rbkmoney.damsel.payment_processing.EventRange;
import com.rbkmoney.hooker.converter.CustomerBindingConverter;
import com.rbkmoney.hooker.converter.CustomerConverter;
import com.rbkmoney.hooker.exception.NotFoundException;
import com.rbkmoney.hooker.exception.RemoteHostException;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.utils.TimeUtils;
import com.rbkmoney.swag_webhook_events.model.*;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerEventService implements EventService<CustomerMessage> {

    private final CustomerManagementSrv.Iface customerClient;
    private final CustomerConverter customerConverter;
    private final CustomerBindingConverter customerBindingConverter;

    @Override
    public Event getByMessage(CustomerMessage message) {
        try {
            Customer customer = customerClient.get(message.getCustomerId(), new EventRange().setLimit(message.getSequenceId().intValue()));
            return resolveEvent(message, customer)
                    .eventID(message.getEventId().intValue())
                    .occuredAt(TimeUtils.toOffsetDateTime(message.getEventTime()))
                    .topic(Event.TopicEnum.CUSTOMERSTOPIC);
        } catch (CustomerNotFound e) {
            throw new NotFoundException("Customer not found, invoiceId=" + message.getCustomerId());
        } catch (TException e) {
            throw new RemoteHostException(e);
        }
    }

    private Event resolveEvent(CustomerMessage message, Customer customer) {
        switch (message.getEventType()) {
            case CUSTOMER_CREATED:
                return new CustomerCreated().customer(customerConverter.convert(customer));
            case CUSTOMER_DELETED:
                return new CustomerDeleted().customer(customerConverter.convert(customer));
            case CUSTOMER_READY:
                return new CustomerReady().customer(customerConverter.convert(customer));
            case CUSTOMER_BINDING_STARTED:
                return new CustomerBindingStarted()
                        .customer(customerConverter.convert(customer))
                        .binding(customerBindingConverter.convert(extractBinding(message, customer)));
            case CUSTOMER_BINDING_SUCCEEDED:
                return new CustomerBindingSucceeded()
                        .customer(customerConverter.convert(customer))
                        .binding(customerBindingConverter.convert(extractBinding(message, customer)));
            case CUSTOMER_BINDING_FAILED:
                return new CustomerBindingFailed()
                        .customer(customerConverter.convert(customer))
                        .binding(customerBindingConverter.convert(extractBinding(message, customer)));
            default:
                throw new UnsupportedOperationException("Unknown event type " + message.getEventType());
        }
    }

    private com.rbkmoney.damsel.payment_processing.CustomerBinding extractBinding(CustomerMessage message, Customer customer) {
        return customer.getBindings().stream()
                .filter(b -> b.getId().equals(message.getBindingId()))
                .findFirst()
                .orElseThrow(() ->
                        new NotFoundException(String.format("Customer binding not found, customerId=%s, bindingId=%s",
                                message.getCustomerId(), message.getBindingId())));
    }
}
