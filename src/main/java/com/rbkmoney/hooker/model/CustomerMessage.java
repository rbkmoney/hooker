package com.rbkmoney.hooker.model;

import com.rbkmoney.hooker.handler.poller.impl.customer.AbstractCustomerEventHandler;
import com.rbkmoney.swag_webhook_events.model.Customer;
import com.rbkmoney.swag_webhook_events.model.CustomerBinding;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by inalarsanukaev on 13.10.17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerMessage extends Message {
    private Long eventId;
    private Long sequenceId;
    private Integer changeId;
    private String type;
    private String occuredAt;
    private String partyId;
    private EventType eventType;
    private Customer customer;
    private CustomerBinding customerBinding;

    public boolean isBinding() {
        return AbstractCustomerEventHandler.BINDING.equals(type);
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
