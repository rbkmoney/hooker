package com.rbkmoney.hooker;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.hooker.dao.CustomerDao;
import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.dao.WebhookAdditionalFilter;
import com.rbkmoney.hooker.handler.poller.impl.customer.AbstractCustomerEventHandler;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.utils.BuildUtils;
import com.rbkmoney.swag_webhook_events.Customer;
import com.rbkmoney.swag_webhook_events.Event;
import com.rbkmoney.swag_webhook_events.PaymentToolDetailsBankCard;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jeckep on 20.04.17.
 */
@TestPropertySource(properties = {"message.scheduler.delay=500"})
public class CustomerDataflowTest extends AbstractIntegrationTest {
    private static Logger log = LoggerFactory.getLogger(CustomerDataflowTest.class);

    @Autowired
    HookDao hookDao;

    @Autowired
    CustomerDao customerDao;

    BlockingQueue<MockMessage> cust1Queue = new LinkedBlockingDeque<>(10);
    BlockingQueue<MockMessage> cust2Queue = new LinkedBlockingDeque<>(10);
    BlockingQueue<MockMessage> cust3Queue = new LinkedBlockingDeque<>(10);

    final List<Hook> hooks = new ArrayList<>();
    final String HOOK_1 = "/hook1";
    final String HOOK_2 = "/hook2";
    final String HOOK_3 = "/hook3";

    String baseServerUrl;


    @Before
    public void setUp() throws Exception {
        //start mock web server
        //createWithPolicy hooks
        if (baseServerUrl == null) {
            baseServerUrl = webserver(dispatcher());
            log.info("Mock server url: " + baseServerUrl);

            hooks.add(hookDao.create(hook("partyId1", "http://" + baseServerUrl + HOOK_1, EventType.CUSTOMER_CREATED)));
            hooks.add(hookDao.create(hook("partyId1", "http://" + baseServerUrl + HOOK_2, EventType.CUSTOMER_READY, EventType.CUSTOMER_CREATED)));
            hooks.add(hookDao.create(hook("partyId2", "http://" + baseServerUrl + HOOK_3, EventType.CUSTOMER_CREATED, EventType.CUSTOMER_READY,  EventType.CUSTOMER_BINDING_STARTED, EventType.CUSTOMER_BINDING_SUCCEEDED)));
        }
    }


    @Test
    public void testMessageSend() throws InterruptedException {
        List<CustomerMessage> sourceMessages = new ArrayList<>();
        CustomerMessage message = BuildUtils.buildCustomerMessage(1L, "partyId1", EventType.CUSTOMER_CREATED, AbstractCustomerEventHandler.CUSTOMER, "1", "2342", Customer.StatusEnum.READY);
        customerDao.create(message);
        sourceMessages.add(message);
        message = BuildUtils.buildCustomerMessage(2L, "partyId1", EventType.CUSTOMER_READY, AbstractCustomerEventHandler.CUSTOMER, "1", "2342", Customer.StatusEnum.READY);
        customerDao.create(message);
        sourceMessages.add(message);
        message = BuildUtils.buildCustomerMessage(3L, "partyId2", EventType.CUSTOMER_CREATED, AbstractCustomerEventHandler.CUSTOMER, "2", "2342", Customer.StatusEnum.READY);
        customerDao.create(message);
        sourceMessages.add(message);
        message = BuildUtils.buildCustomerMessage(4L, "partyId2", EventType.CUSTOMER_READY, AbstractCustomerEventHandler.CUSTOMER, "2", "2342", Customer.StatusEnum.READY);
        customerDao.create(message);
        sourceMessages.add(message);
        message = BuildUtils.buildCustomerMessage(5L, "partyId2", EventType.CUSTOMER_BINDING_STARTED, AbstractCustomerEventHandler.BINDING, "2", "2342", Customer.StatusEnum.READY);
        customerDao.create(message);
        sourceMessages.add(message);
        message = BuildUtils.buildCustomerMessage(6L, "partyId2", EventType.CUSTOMER_BINDING_SUCCEEDED, AbstractCustomerEventHandler.BINDING, "3", "2342", Customer.StatusEnum.READY);
        customerDao.create(message);
        sourceMessages.add(message);

        List<MockMessage> cust1 = new ArrayList<>();
        List<MockMessage> cust2 = new ArrayList<>();
        List<MockMessage> cust3 = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            cust1.add(cust1Queue.poll(1, TimeUnit.SECONDS));
        }
        for (int i = 0; i < 3; i++) {
            Assert.assertNotNull(cust1.get(i));
        }


        for (int i = 0; i < 3; i++) {
            cust2.add(cust2Queue.poll(1, TimeUnit.SECONDS));
        }
        for (int i = 0; i < 3; i++) {
            Assert.assertNotNull(cust2.get(i));
        }
        assertEquals(sourceMessages.get(2).getEventId(), cust2.get(0).getEventID());
        assertEquals(sourceMessages.get(3).getEventId(), cust2.get(1).getEventID());
        assertEquals(sourceMessages.get(4).getEventId(), cust2.get(2).getEventID());


        cust3.add(cust3Queue.poll(1, TimeUnit.SECONDS));
        Assert.assertNotNull(cust3.get(0));
        assertEquals(sourceMessages.get(5).getEventId(), cust3.get(0).getEventID());

        assertTrue(cust1Queue.isEmpty());
        assertTrue(cust2Queue.isEmpty());
        assertTrue(cust3Queue.isEmpty());

        Thread.currentThread().sleep(1000);

    }

    private static Hook hook(String partyId, String url, EventType... types) {
        Hook hook = new Hook();
        hook.setPartyId(partyId);
        hook.setTopic(Event.TopicEnum.CUSTOMERSTOPIC.getValue());
        hook.setUrl(url);

        Set<WebhookAdditionalFilter> webhookAdditionalFilters = new HashSet<>();
        for (EventType type : types) {
            webhookAdditionalFilters.add(new WebhookAdditionalFilter(type));
        }
        hook.setFilters(webhookAdditionalFilters);

        return hook;
    }

    private Dispatcher dispatcher() {
        final Dispatcher dispatcher = new Dispatcher() {

            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                MockMessage mockMessage = extract(request);
                String customerId = mockMessage.getCustomer().getId();
                switch (customerId) {
                    case "1":
                        cust1Queue.put(mockMessage);
                        break;
                    case "2":
                        cust2Queue.put(mockMessage);
                        break;
                    case "3":
                        cust3Queue.put(mockMessage);
                        break;
                    default:
                        Thread.sleep(100);
                        return new MockResponse().setResponseCode(500);
                }

                Thread.sleep(100);
                return new MockResponse().setBody(HOOK_1).setResponseCode(200);
            }
        };
        return dispatcher;
    }

    private String webserver(Dispatcher dispatcher) {
        final MockWebServer server = new MockWebServer();
        server.setDispatcher(dispatcher);
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.info("Mock Hook Server started on port: " + server.getPort());

        // process request
        new Thread(() -> {
            while (true) {
                try {
                    server.takeRequest();
                } catch (InterruptedException e) {
                    try {
                        server.shutdown();
                    } catch (IOException e1) {
                        new RuntimeException(e1);
                    }
                }
            }
        }).start();


        return server.getHostName() + ":" + server.getPort();
    }

    public static MockMessage extract(RecordedRequest request) {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            request.getBody().writeTo(bout);
            return new ObjectMapper()
                    .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true)
                    .readValue(bout.toByteArray(), MockMessage.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class MockMessage {
        private Long eventID;
        private String occuredAt;
        private String topic;
        private String eventType;
        private Customer customer;
        private CustomerBinding binding;

        public Long getEventID() {
            return eventID;
        }

        public void setEventID(Long eventID) {
            this.eventID = eventID;
        }

        public String getOccuredAt() {
            return occuredAt;
        }

        public void setOccuredAt(String occuredAt) {
            this.occuredAt = occuredAt;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public Customer getCustomer() {
            return customer;
        }

        public void setCustomer(Customer customer) {
            this.customer = customer;
        }

        public CustomerBinding getBinding() {
            return binding;
        }

        public void setBinding(CustomerBinding binding) {
            this.binding = binding;
        }

        public class CustomerBinding extends com.rbkmoney.swag_webhook_events.CustomerBinding {
            private PaymentResource paymentResource;

            @Override
            public PaymentResource getPaymentResource() {
                return paymentResource;
            }

            public void setPaymentResource(PaymentResource paymentResource) {
                this.paymentResource = paymentResource;
            }

            public class PaymentResource extends com.rbkmoney.swag_webhook_events.PaymentResource {
                private PaymentToolDetailsBankCard paymentToolDetails;

                @Override
                public PaymentToolDetailsBankCard getPaymentToolDetails() {
                    return paymentToolDetails;
                }

                public void setPaymentToolDetails(PaymentToolDetailsBankCard paymentToolDetails) {
                    this.paymentToolDetails = paymentToolDetails;
                }
            }
        }
    }
}
