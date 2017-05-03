package com.rbkmoney.hooker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.dao.SimpleRetryPolicyDao;
import com.rbkmoney.hooker.dao.WebhookAdditionalFilter;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.retry.impl.simple.SimpleRetryPolicyRecord;
import lombok.Data;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
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
    BlockingQueue<MockMessage> hookBrokenQueue = new LinkedBlockingDeque<>(10);

    final List<Hook> hooks = new ArrayList<>();
    final String HOOK_1 = "/hook1";
    final String HOOK_2 = "/hook2";
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
        }
    }


    @Test
    public void testMessageSend() throws InterruptedException {
        List<Message> sourceMessages = new ArrayList<>();
        sourceMessages.add(messageDao.create(message("1", "partyId1", EventType.INVOICE_CREATED, "status")));
        sourceMessages.add(messageDao.create(message("2", "partyId1", EventType.INVOICE_PAYMENT_STARTED, "status")));
        sourceMessages.add(messageDao.create(message("3", "partyId1", EventType.INVOICE_CREATED, "status")));

        sourceMessages.add(messageDao.create(message("4", "qwe", EventType.INVOICE_CREATED, "status")));
        sourceMessages.add(messageDao.create(message("5", "qwe", EventType.INVOICE_CREATED, "status")));

        List<MockMessage> hook1 = new ArrayList<>();
        List<MockMessage> hook2 = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            hook1.add(hook1Queue.poll(1, TimeUnit.SECONDS));
        }
        assertEquals(sourceMessages.get(0).getInvoiceId(), hook1.get(0).getPayload().getInvoiceId());
        assertEquals(sourceMessages.get(2).getInvoiceId(), hook1.get(1).getPayload().getInvoiceId());


        for (int i = 0; i < 3; i++) {
            hook2.add(hook2Queue.poll(1, TimeUnit.SECONDS));
        }
        for (int i = 0; i < 3; i++) {
            assertEquals(sourceMessages.get(i).getInvoiceId(), hook2.get(i).getPayload().getInvoiceId());
        }

        assertTrue(hook1Queue.isEmpty());
        assertTrue(hook2Queue.isEmpty());

        Thread.currentThread().sleep(1000);

    }

    @Test
    public void testDisableHookPolicy() throws InterruptedException {
        final String invoceId = "asgsdhghdhtfugny648";
        final String partyId = new Random().nextInt() + "";
        Hook hook = hookDao.create(hook(partyId, "http://" + baseServerUrl + BROKEN_HOOK, EventType.INVOICE_CREATED));
        simpleRetryPolicyDao.update(new SimpleRetryPolicyRecord(hook.getId(), 3, 0));

        Message message = messageDao.create(message(invoceId, partyId, EventType.INVOICE_CREATED, "status"));
        assertEquals(message.getInvoiceId(), hookBrokenQueue.poll(1, TimeUnit.SECONDS).getPayload().getInvoiceId());

        Thread.sleep(1000);

        hook = hookDao.getHookById(hook.getId());
        assertFalse(hook.isEnabled());
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

    private static MockMessage extract(RecordedRequest request) {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            request.getBody().writeTo(bout);
            return new ObjectMapper().readValue(bout.toByteArray(), MockMessage.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Data
    private static class MockMessage {
        private long eventId;
        private String eventTime;
        private String eventType;
        private MockPayload payload;
    }

    @Data
    private static class MockPayload {
        private String payloadType;
        private long amount;
        private String createdAt;
        private String currency;
        private String invoiceId;
        private Content metadata;
        private int shopId;
        private String partyId;
        private String status;
        private String product;
        private String description;
        private String paymentId;
    }

    @Data
    private static class Content {
        public String type;
        public byte[] data;
    }

}
