package com.rbkmoney.hooker;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.hooker.dao.CustomerDao;
import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.dao.SimpleRetryPolicyDao;
import com.rbkmoney.hooker.dao.WebhookAdditionalFilter;
import com.rbkmoney.hooker.handler.poller.impl.customer.AbstractCustomerEventHandler;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.utils.BuildUtils;
import com.rbkmoney.swag_webhook_events.Customer;
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
@TestPropertySource(properties = {"message.scheduler.delay=100"})
public class CustomerDataflowTest extends AbstractIntegrationTest {
    private static Logger log = LoggerFactory.getLogger(CustomerDataflowTest.class);

    @Autowired
    HookDao hookDao;

    @Autowired
    CustomerDao customerDao;

    @Autowired
    SimpleRetryPolicyDao simpleRetryPolicyDao;

    BlockingQueue<MockMessage> hook1Queue = new LinkedBlockingDeque<>(10);
    BlockingQueue<MockMessage> hook2Queue = new LinkedBlockingDeque<>(10);
    BlockingQueue<MockMessage> hook3Queue = new LinkedBlockingDeque<>(10);
    BlockingQueue<MockMessage> hookBrokenQueue = new LinkedBlockingDeque<>(10);

    final List<Hook> hooks = new ArrayList<>();
    final String HOOK_1 = "/hook1";
    final String HOOK_2 = "/hook2";
    final String HOOK_3 = "/hook3";
    final String BROKEN_HOOK = "/brokenhook";

    String baseServerUrl;


    @Before
    public void setUp() throws Exception {
        //start mock web server
        //create hooks
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
        sourceMessages.add(customerDao.create(BuildUtils.buildCustomerMessage(1L, "partyId1",  EventType.CUSTOMER_CREATED, AbstractCustomerEventHandler.CUSTOMER, "1234", "2342", Customer.StatusEnum.READY)));
        sourceMessages.add(customerDao.create(BuildUtils.buildCustomerMessage(2L, "partyId1",  EventType.CUSTOMER_READY, AbstractCustomerEventHandler.CUSTOMER, "1234", "2342", Customer.StatusEnum.READY)));
        sourceMessages.add(customerDao.create(BuildUtils.buildCustomerMessage(3L, "partyId2",  EventType.CUSTOMER_CREATED, AbstractCustomerEventHandler.CUSTOMER, "666", "2342", Customer.StatusEnum.READY)));
        sourceMessages.add(customerDao.create(BuildUtils.buildCustomerMessage(4L, "partyId2",  EventType.CUSTOMER_READY, AbstractCustomerEventHandler.CUSTOMER, "6666", "2342", Customer.StatusEnum.READY)));
        sourceMessages.add(customerDao.create(BuildUtils.buildCustomerMessage(5L, "partyId2",  EventType.CUSTOMER_BINDING_STARTED, AbstractCustomerEventHandler.BINDING, "6666", "2342", Customer.StatusEnum.READY)));
        sourceMessages.add(customerDao.create(BuildUtils.buildCustomerMessage(6L, "partyId2",  EventType.CUSTOMER_BINDING_SUCCEEDED, AbstractCustomerEventHandler.BINDING, "4444", "2342", Customer.StatusEnum.READY)));

        List<MockMessage> hook1 = new ArrayList<>();
        List<MockMessage> hook2 = new ArrayList<>();
        List<MockMessage> hook3 = new ArrayList<>();

        for (int i = 0; i < 1; i++) {
            hook1.add(hook1Queue.poll(1, TimeUnit.SECONDS));
        }
        Assert.assertNotNull(hook1.get(0));
        assertEquals(sourceMessages.get(0).getEventId(), hook1.get(0).getEventID());

        for (int i = 0; i < 2; i++) {
            hook2.add(hook2Queue.poll(1, TimeUnit.SECONDS));
        }
        Assert.assertEquals(hook2.size(), 2);
        assertEquals(sourceMessages.get(0).getEventId(), hook2.get(0).getEventID());
        assertEquals(sourceMessages.get(1).getEventId(), hook2.get(1).getEventID());

        for (int i = 0; i < 4; i++) {
            hook3.add(hook3Queue.poll(1, TimeUnit.SECONDS));
        }

        assertEquals(hook3.size(), 4);

        assertTrue(hook1Queue.isEmpty());
        assertTrue(hook2Queue.isEmpty());
        assertTrue(hook3Queue.isEmpty());

        Thread.currentThread().sleep(1000);

    }

    private static Hook hook(String partyId, String url, EventType... types) {
        Hook hook = new Hook();
        hook.setPartyId(partyId);
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
                if (request.getPath().startsWith(HOOK_1)) {
                    hook1Queue.put(extract(request));
                    Thread.sleep(100);
                    return new MockResponse().setBody(HOOK_1).setResponseCode(200);
                }
                if (request.getPath().startsWith(HOOK_2)) {
                    hook2Queue.put(extract(request));
                    Thread.sleep(100);
                    return new MockResponse().setBody(HOOK_2).setResponseCode(200);
                }
                if (request.getPath().startsWith(HOOK_3)) {
                    hook3Queue.put(extract(request));
                    Thread.sleep(100);
                    return new MockResponse().setBody(HOOK_3).setResponseCode(200);
                }
                if (request.getPath().startsWith(BROKEN_HOOK)) {
                    hookBrokenQueue.put(extract(request));
                    Thread.sleep(100);
                    return new MockResponse().setBody(BROKEN_HOOK).setResponseCode(500);
                }

                return new MockResponse().setResponseCode(500);
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
        private long eventID;
        private String occuredAt;
        private String topic;
        private String eventType;
        private Customer customer;
        private CustomerBinding binding;

        public long getEventID() {
            return eventID;
        }

        public void setEventID(long eventID) {
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
