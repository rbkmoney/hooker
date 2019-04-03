package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rbkmoney.swag_webhook_events.Customer;
import com.rbkmoney.swag_webhook_events.CustomerBinding;
import com.rbkmoney.swag_webhook_events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by inalarsanukaev on 07.04.17.
 */
@JsonPropertyOrder({"eventID", "occuredAt", "topic", "eventType", "customer"})
@Getter
@Setter
public class CustomerMessageJson {
    private static Map<EventType, Event.EventTypeEnum> eventTypeMapping = new HashMap<>();

    static {
        eventTypeMapping.put(EventType.CUSTOMER_CREATED, Event.EventTypeEnum.CUSTOMERCREATED);
        eventTypeMapping.put(EventType.CUSTOMER_READY, Event.EventTypeEnum.CUSTOMERREADY);
        eventTypeMapping.put(EventType.CUSTOMER_DELETED, Event.EventTypeEnum.CUSTOMERDELETED);
        eventTypeMapping.put(EventType.CUSTOMER_BINDING_STARTED, Event.EventTypeEnum.CUSTOMERBINDINGSTARTED);
        eventTypeMapping.put(EventType.CUSTOMER_BINDING_SUCCEEDED, Event.EventTypeEnum.CUSTOMERBINDINGSUCCEEDED);
        eventTypeMapping.put(EventType.CUSTOMER_BINDING_FAILED, Event.EventTypeEnum.CUSTOMERBINDINGFAILED);
    }

    private long eventID;
    private String occuredAt;
    private String topic;
    private String eventType;
    private Customer customer;

    public static String buildMessageJson(CustomerMessage message) throws JsonProcessingException {
        CustomerMessageJson messageJson =  message.isBinding() ? new CustomerBindingMessageJson(message.getCustomerBinding()) : new CustomerMessageJson();
        messageJson.eventID = message.getEventId();
        messageJson.occuredAt = message.getOccuredAt();
        messageJson.topic = Event.TopicEnum.CUSTOMERSTOPIC.getValue();
        messageJson.customer = message.getCustomer();
        messageJson.eventType = eventTypeMapping.get(message.getEventType()).getValue();
        return new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
                .writeValueAsString(messageJson);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class CustomerBindingMessageJson extends CustomerMessageJson{
        private CustomerBinding binding;
    }

}

