package com.rbkmoney.hooker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.payment_processing.CustomerManagementSrv;
import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.model.*;
import com.rbkmoney.hooker.utils.BuildUtils;
import com.rbkmoney.swag_webhook_events.model.CustomerBindingSucceeded;
import com.rbkmoney.swag_webhook_events.model.Event;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;

public class CustomerEventServiceTest extends AbstractIntegrationTest {

    @MockBean
    private CustomerManagementSrv.Iface customerClient;

    @Autowired
    private CustomerEventService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        Mockito.when(customerClient.get(any(), any()))
                .thenReturn(BuildUtils.buildCustomer("customerId", "bindingId"));
    }

    @Test
    public void testCustomerSucceded() {
        CustomerMessage message = random(CustomerMessage.class);
        message.setType(CustomerMessageEnum.BINDING);
        message.setEventTime("2016-03-22T06:12:27Z");
        message.setEventType(EventType.CUSTOMER_BINDING_SUCCEEDED);
        message.setBindingId("bindingId");
        Event event = service.getEventByMessage(message);
        assertTrue(event instanceof CustomerBindingSucceeded);
        CustomerBindingSucceeded bindingSucceeded = (CustomerBindingSucceeded) event;
        assertEquals(message.getEventId().intValue(), event.getEventID().intValue());
        assertEquals("customerId", bindingSucceeded.getCustomer().getId());
        assertEquals("bindingId", bindingSucceeded.getBinding().getId());
    }

    @Test
    public void testJson() throws JsonProcessingException {
        CustomerMessage message = random(CustomerMessage.class);
        message.setType(CustomerMessageEnum.BINDING);
        message.setEventTime("2016-03-22T06:12:27Z");
        message.setEventType(EventType.CUSTOMER_BINDING_FAILED);
        message.setBindingId("bindingId");
        Event event = service.getEventByMessage(message);
        String json = objectMapper.writeValueAsString(event);
        assertTrue(json.contains("2016-03-22T06:12:27Z"));
    }
}