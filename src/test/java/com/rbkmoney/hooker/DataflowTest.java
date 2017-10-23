package com.rbkmoney.hooker;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.dao.SimpleRetryPolicyDao;
import com.rbkmoney.hooker.dao.WebhookAdditionalFilter;
import com.rbkmoney.hooker.handler.poller.impl.invoicing.AbstractInvoiceEventHandler;
import com.rbkmoney.hooker.model.*;
import com.rbkmoney.hooker.retry.impl.simple.SimpleRetryPolicyRecord;
import com.rbkmoney.swag_webhook_events.CustomerPayer;
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
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static com.rbkmoney.hooker.utils.BuildUtils.cart;
import static com.rbkmoney.hooker.utils.BuildUtils.message;
import static org.junit.Assert.*;

/**
 * Created by jeckep on 20.04.17.
 */
@TestPropertySource(properties = {"message.scheduler.delay=100"})
public class DataflowTest extends AbstractIntegrationTest {
    private static Logger log = LoggerFactory.getLogger(DataflowTest.class);

    @Autowired
    HookDao hookDao;

    @Autowired
    MessageDao messageDao;

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

            hooks.add(hookDao.create(hook("partyId1", "http://" + baseServerUrl + HOOK_1, EventType.INVOICE_CREATED)));
            hooks.add(hookDao.create(hook("partyId1", "http://" + baseServerUrl + HOOK_2, EventType.INVOICE_CREATED, EventType.INVOICE_PAYMENT_STARTED)));
            hooks.add(hookDao.create(hook("partyId2", "http://" + baseServerUrl + HOOK_3, EventType.INVOICE_PAYMENT_STATUS_CHANGED)));
        }
    }

    @Test
    public void testCache(){
        final String invoceId = "asgsdhghdhtfugny78989";
        final String partyId = new Random().nextInt() + "";
        Message message1 = messageDao.create(message(AbstractInvoiceEventHandler.INVOICE, invoceId, partyId, EventType.INVOICE_CREATED, "status"));
        Message message2 = messageDao.getAny(invoceId, AbstractInvoiceEventHandler.INVOICE);
        Message message3 = messageDao.getAny(invoceId, AbstractInvoiceEventHandler.INVOICE);
        assertTrue(message1 != message2);
        assertTrue(message2 != message3);
        assertTrue(message1 != message3);
    }

    @Test
    public void testMessageSend() throws InterruptedException {
        List<Message> sourceMessages = new ArrayList<>();
        sourceMessages.add(messageDao.create(message(AbstractInvoiceEventHandler.INVOICE,"1", "partyId1", EventType.INVOICE_CREATED, "status", cart(), true)));
        sourceMessages.add(messageDao.create(message(AbstractInvoiceEventHandler.PAYMENT,"2", "partyId1", EventType.INVOICE_PAYMENT_STARTED, "status")));
        sourceMessages.add(messageDao.create(message(AbstractInvoiceEventHandler.INVOICE,"3", "partyId1", EventType.INVOICE_CREATED, "status")));

        sourceMessages.add(messageDao.create(message(AbstractInvoiceEventHandler.INVOICE,"4", "qwe", EventType.INVOICE_CREATED, "status")));
        sourceMessages.add(messageDao.create(message(AbstractInvoiceEventHandler.INVOICE,"5", "qwe", EventType.INVOICE_CREATED, "status")));

        sourceMessages.add(messageDao.create(message(AbstractInvoiceEventHandler.PAYMENT,"6", "partyId2", EventType.INVOICE_PAYMENT_STATUS_CHANGED, "status", cart(), false)));

        List<MockMessage> hook1 = new ArrayList<>();
        List<MockMessage> hook2 = new ArrayList<>();
        List<MockMessage> hook3 = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            hook1.add(hook1Queue.poll(1, TimeUnit.SECONDS));
        }
        Assert.assertNotNull(hook1.get(0));
        Assert.assertNotNull(hook1.get(1));
        assertEquals(sourceMessages.get(0).getInvoice().getId(), hook1.get(0).getInvoice().getId());
        assertEquals(sourceMessages.get(2).getInvoice().getId(), hook1.get(1).getInvoice().getId());


        for (int i = 0; i < 3; i++) {
            hook2.add(hook2Queue.poll(1, TimeUnit.SECONDS));
        }
        for (int i = 0; i < 3; i++) {
            assertEquals(sourceMessages.get(i).getInvoice().getId(), hook2.get(i).getInvoice().getId());
        }

        for (int i = 0; i < 1; i++) {
            hook3.add(hook3Queue.poll(1, TimeUnit.SECONDS));
        }
        assertTrue(hook3.get(0).getPayment().getPayer() instanceof CustomerPayer);

        assertTrue(hook1Queue.isEmpty());
        assertTrue(hook2Queue.isEmpty());
        assertTrue(hook3Queue.isEmpty());

        Thread.currentThread().sleep(1000);

    }

    @Test
    public void testDisableHookPolicy() throws InterruptedException {
        final String invoceId = "asgsdhghdhtfugny648";
        final String partyId = new Random().nextInt() + "";
        Hook hook = hookDao.create(hook(partyId, "http://" + baseServerUrl + BROKEN_HOOK, EventType.INVOICE_CREATED));
        simpleRetryPolicyDao.update(new SimpleRetryPolicyRecord(hook.getId(), 4, 0));

        Message message = messageDao.create(message(AbstractInvoiceEventHandler.INVOICE, invoceId, partyId, EventType.INVOICE_CREATED, "status"));
        assertEquals(message.getInvoice().getId(), hookBrokenQueue.poll(1, TimeUnit.SECONDS).getInvoice().getId());

        Thread.sleep(1000);

        hook = hookDao.getHookById(hook.getId());
        assertTrue(hook.isEnabled());
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
                    hook1Queue.put(extractPaymentResourcePayer(request));
                    Thread.sleep(100);
                    return new MockResponse().setBody(HOOK_1).setResponseCode(200);
                }
                if (request.getPath().startsWith(HOOK_2)) {
                    hook2Queue.put(extractPaymentResourcePayer(request));
                    Thread.sleep(100);
                    return new MockResponse().setBody(HOOK_2).setResponseCode(200);
                }
                if (request.getPath().startsWith(HOOK_3)) {
                    hook3Queue.put(extractCustomerPayer(request));
                    Thread.sleep(100);
                    return new MockResponse().setBody(HOOK_3).setResponseCode(200);
                }
                if (request.getPath().startsWith(BROKEN_HOOK)) {
                    hookBrokenQueue.put(extractPaymentResourcePayer(request));
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

    public static MockMessage extractPaymentResourcePayer(RecordedRequest request) {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            request.getBody().writeTo(bout);
            return new ObjectMapper()
                    .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true)
                    .readValue(bout.toByteArray(), MockMessagePaymentResource.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MockMessage extractCustomerPayer(RecordedRequest request) {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            request.getBody().writeTo(bout);
            return new ObjectMapper()
                    .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true)
                    .readValue(bout.toByteArray(), MockMessageCustomer.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class MockMessagePaymentResource extends MockMessage {
        private Payment payment;

        @Override
        public Payment getPayment() {
            return payment;
        }

        public void setPayment(Payment payment) {
            this.payment = payment;
        }

        public class Payment extends com.rbkmoney.hooker.model.Payment{
            private PaymentResourcePayer payer;

            @Override
            public PaymentResourcePayer getPayer() {
                return payer;
            }

            public void setPayer(PaymentResourcePayer payer) {
                this.payer = payer;
            }

            public class PaymentResourcePayer extends com.rbkmoney.swag_webhook_events.PaymentResourcePayer {
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

    public static class MockMessageCustomer extends MockMessage {
        private Payment payment;

        @Override
        public Payment getPayment() {
            return payment;
        }

        public void setPayment(Payment payment) {
            this.payment = payment;
        }

        public class Payment extends com.rbkmoney.hooker.model.Payment{
            private CustomerPayer payer;

            @Override
            public CustomerPayer getPayer() {
                return payer;
            }

            public void setPayer(CustomerPayer payer) {
                this.payer = payer;
            }
        }
    }

    public static class MockMessage {
        private long eventID;
        private String occuredAt;
        private String topic;
        private String eventType;
        private Invoice invoice;
        private Payment payment;

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

        public Invoice getInvoice() {
            return invoice;
        }

        public void setInvoice(Invoice invoice) {
            this.invoice = invoice;
        }

        public Payment getPayment() {
            return payment;
        }

        public void setPayment(Payment payment) {
            this.payment = payment;
        }
    }
}
