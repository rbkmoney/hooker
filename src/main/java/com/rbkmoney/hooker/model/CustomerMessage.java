package com.rbkmoney.hooker.model;

import com.rbkmoney.hooker.handler.poller.impl.customer.AbstractCustomerEventHandler;
import com.rbkmoney.swag_webhook_events.Customer;
import com.rbkmoney.swag_webhook_events.CustomerBinding;

/**
 * Created by inalarsanukaev on 13.10.17.
 */
public class CustomerMessage extends Message {
    private long eventId;
    private String type;
    private String occuredAt;
    private String partyId;
    private EventType eventType;
    private Customer customer;
    private CustomerBinding customerBinding;

    public boolean isBinding() {
        return AbstractCustomerEventHandler.BINDING.equals(type);
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOccuredAt() {
        return occuredAt;
    }

    public void setOccuredAt(String occuredAt) {
        this.occuredAt = occuredAt;
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public CustomerBinding getCustomerBinding() {
        return customerBinding;
    }

    public void setCustomerBinding(CustomerBinding customerBinding) {
        this.customerBinding = customerBinding;
    }

    @Override
    public String toString() {
        return "CustomerMessage{" +
                "id=" + getId() +
                ", eventId=" + eventId +
                ", type='" + type +
                ", occuredAt='" + occuredAt +
                ", partyId='" + partyId +
                ", eventType=" + eventType +
                ", customer=" + customer.toString().replaceAll("\n", "") +
                (isBinding() ? ", customerBinding=" + customerBinding.toString().replaceAll("\n", "") : "") +
                '}';
    }
}
