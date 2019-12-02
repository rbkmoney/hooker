package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.payment_processing.Customer;
import com.rbkmoney.damsel.payment_processing.CustomerManagementSrv;
import com.rbkmoney.damsel.payment_processing.CustomerNotFound;
import com.rbkmoney.damsel.payment_processing.EventRange;
import com.rbkmoney.hooker.configuration.meta.UserIdentityIdExtensionKit;
import com.rbkmoney.hooker.configuration.meta.UserIdentityRealmExtensionKit;
import com.rbkmoney.hooker.converter.CustomerBindingConverter;
import com.rbkmoney.hooker.converter.CustomerConverter;
import com.rbkmoney.hooker.exception.NotFoundException;
import com.rbkmoney.hooker.exception.RemoteHostException;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.utils.TimeUtils;
import com.rbkmoney.swag_webhook_events.model.*;
import com.rbkmoney.woody.api.flow.WFlow;
import com.rbkmoney.woody.api.trace.ContextUtils;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerEventService implements EventService<CustomerMessage> {

    private final CustomerManagementSrv.Iface customerClient;
    private final CustomerConverter customerConverter;
    private final CustomerBindingConverter customerBindingConverter;
    private final WFlow wFlow = new WFlow();

    @Override
    public Event getByMessage(CustomerMessage message) {
        try {
            Customer customer = wFlow.createServiceFork(() -> {
                        addWoodyContext();
                        return customerClient.get(message.getCustomerId(),
                                getEventRange(message.getSequenceId().intValue()));
                    }
            ).call();

            return resolveEvent(message, customer)
                    .eventID(message.getEventId().intValue())
                    .occuredAt(TimeUtils.toOffsetDateTime(message.getEventTime()))
                    .topic(Event.TopicEnum.CUSTOMERSTOPIC);
        } catch (CustomerNotFound e) {
            throw new NotFoundException("Customer not found, invoiceId=" + message.getCustomerId());
        } catch (Exception e) {
            throw new RemoteHostException(e);
        }
    }

    private void addWoodyContext(){
        ContextUtils.setCustomMetadataValue(UserIdentityIdExtensionKit.KEY, "hooker");
        ContextUtils.setCustomMetadataValue(UserIdentityRealmExtensionKit.KEY, "service");
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
