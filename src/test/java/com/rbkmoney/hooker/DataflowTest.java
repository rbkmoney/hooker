package com.rbkmoney.hooker;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.dao.WebhookAdditionalFilter;
import com.rbkmoney.hooker.handler.poller.impl.invoicing.AbstractInvoiceEventHandler;
import com.rbkmoney.hooker.model.*;
import com.rbkmoney.swag_webhook_events.model.CustomerPayer;
import com.rbkmoney.swag_webhook_events.model.Event;
import com.rbkmoney.swag_webhook_events.model.PaymentToolDetailsBankCard;
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

import static com.rbkmoney.hooker.utils.BuildUtils.buildMessage;
import static com.rbkmoney.hooker.utils.BuildUtils.cart;
import static org.junit.Assert.*;

/**
 * Created by jeckep on 20.04.17.
 */
@TestPropertySource(properties = {"message.scheduler.delay=500"})
public class DataflowTest extends AbstractIntegrationTest {
    private static Logger log = LoggerFactory.getLogger(DataflowTest.class);

    @Autowired
    HookDao hookDao;

    @Autowired
    InvoicingMessageDao messageDao;

    BlockingQueue<MockMessage> inv1Queue = new LinkedBlockingDeque<>(10);
    BlockingQueue<MockMessage> inv3Queue = new LinkedBlockingDeque<>(10);
    BlockingQueue<MockMessage> inv4Queue = new LinkedBlockingDeque<>(10);
    BlockingQueue<MockMessage> inv5Queue = new LinkedBlockingDeque<>(10);

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

            hooks.add(hookDao.create(hook("partyId1", "http://" + baseServerUrl + HOOK_1, EventType.INVOICE_CREATED)));
            hooks.add(hookDao.create(hook("partyId1", "http://" + baseServerUrl + HOOK_2, EventType.INVOICE_CREATED, EventType.INVOICE_PAYMENT_STARTED)));
            hooks.add(hookDao.create(hook("partyId2", "http://" + baseServerUrl + HOOK_3, EventType.INVOICE_PAYMENT_STATUS_CHANGED, EventType.INVOICE_PAYMENT_REFUND_STARTED)));
        }
    }

    @Test
    public void testMessageSend() throws InterruptedException {
        List<InvoicingMessage> sourceMessages = new ArrayList<>();
        InvoicingMessage message = buildMessage(AbstractInvoiceEventHandler.INVOICE, "1", "partyId1", EventType.INVOICE_CREATED, "status", cart(), true, 0L, 0);
        messageDao.create(message);
        sourceMessages.add(message);
        message = buildMessage(AbstractInvoiceEventHandler.PAYMENT, "1", "partyId1", EventType.INVOICE_PAYMENT_STARTED, "status", cart(), true, 0L, 1);
        messageDao.create(message);
        sourceMessages.add(message);
        message = buildMessage(AbstractInvoiceEventHandler.INVOICE,"3", "partyId1", EventType.INVOICE_CREATED, "status");
        messageDao.create(message);
        sourceMessages.add(message);
        message = buildMessage(AbstractInvoiceEventHandler.INVOICE, "4", "qwe", EventType.INVOICE_CREATED, "status");
        messageDao.create(message);
        sourceMessages.add(message);
        message = buildMessage(AbstractInvoiceEventHandler.INVOICE, "5", "partyId2", EventType.INVOICE_CREATED, "status", cart(), false, 0L, 0);
        messageDao.create(message);
        sourceMessages.add(message);
        message = buildMessage(AbstractInvoiceEventHandler.PAYMENT, "5", "partyId2", EventType.INVOICE_PAYMENT_STATUS_CHANGED, "status", cart(), false, 0L, 1);
        messageDao.create(message);
        sourceMessages.add(message);
        message = buildMessage(AbstractInvoiceEventHandler.REFUND, "5", "partyId2", EventType.INVOICE_PAYMENT_REFUND_STARTED, "status", cart(), false, 0L, 2);
        messageDao.create(message);
        sourceMessages.add(message);
        message = buildMessage(AbstractInvoiceEventHandler.PAYMENT, "5", "partyId2", EventType.INVOICE_PAYMENT_CASH_FLOW_CHANGED, "status", cart(), false, 0L, 1);
        messageDao.create(message);
        sourceMessages.add(message);

        List<MockMessage> inv1 = new ArrayList<>();
        List<MockMessage> inv3 = new ArrayList<>();
        List<MockMessage> inv4 = new ArrayList<>();
        List<MockMessage> inv5 = new ArrayList<>();

        Thread.currentThread().sleep(1000);

        for (int i = 0; i < 3; i++) {
            inv1.add(inv1Queue.poll(1, TimeUnit.SECONDS));
        }
        assertNotNull(inv1.get(0));
        assertNotNull(inv1.get(1));
        assertNotNull(inv1.get(2));

        for (int i = 0; i < 2; i++) {
            inv3.add(inv3Queue.poll(1, TimeUnit.SECONDS));
        }
        assertNotNull(inv3.get(0));
        assertNotNull(inv3.get(1));

        inv4.add(inv4Queue.poll(1, TimeUnit.SECONDS));
        Assert.assertNull(inv4.get(0));

        inv5.add(inv5Queue.poll(1, TimeUnit.SECONDS));
        inv5.add(inv5Queue.poll(1, TimeUnit.SECONDS));
        assertNotNull(inv5.get(0));
        assertEquals(sourceMessages.get(5).getInvoice().getId(), inv5.get(0).getInvoice().getId());
        assertTrue(inv5.get(0).getPayment().getPayer() instanceof CustomerPayer);
        assertNotNull(inv5.get(1).getRefund());

        assertTrue(inv1Queue.isEmpty());
        assertTrue(inv3Queue.isEmpty());
        assertTrue(inv4Queue.isEmpty());
        assertTrue(inv5Queue.isEmpty());

    }

    private static Hook hook(String partyId, String url, EventType... types) {
        Hook hook = new Hook();
        hook.setPartyId(partyId);
        hook.setTopic(Event.TopicEnum.INVOICESTOPIC.getValue());
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
                MockMessage mockMessage;
                if (request.getPath().startsWith(HOOK_3)) {
                    mockMessage = extractCustomerPayer(request);
                } else {
                    mockMessage = extractPaymentResourcePayer(request);
                }

                String invoiceId = mockMessage.getInvoice().getId();
                switch (invoiceId) {
                    case "1":
                        inv1Queue.put(mockMessage);
                        break;
                    case "3":
                        inv3Queue.put(mockMessage);
                        break;
                    case "4":
                        inv4Queue.put(mockMessage);
                        break;
                    case "5":
                        inv5Queue.put(mockMessage);
                        break;
                    default:
                        Thread.sleep(100);
                        return new MockResponse().setBody("FAIL").setResponseCode(500);
                }

                Thread.sleep(100);
                return new MockResponse().setBody("OK").setResponseCode(200);
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

            public class PaymentResourcePayer extends com.rbkmoney.swag_webhook_events.model.PaymentResourcePayer {
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
        private Refund refund;

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

        public Refund getRefund() {
            return refund;
        }

        public void setRefund(Refund refund) {
            this.refund = refund;
        }
    }
}
