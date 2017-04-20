package com.rbkmoney.hooker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.dao.WebhookAdditionalFilter;
import com.rbkmoney.hooker.dao.WebhookDao;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.model.Message;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

public class DataflowTest extends AbstractIntegrationTest {
    private static Logger log = LoggerFactory.getLogger(DataflowTest.class);

    @Autowired
    WebhookDao webhookDao;

    @Autowired
    MessageDao messageDao;

    BlockingQueue<Message>  hook1Queue = new LinkedBlockingDeque<>(10);
    BlockingQueue<Message>  hook2Queue = new LinkedBlockingDeque<>(10);

    final List<Hook> hooks = new ArrayList<>();
    final String HOOK_1 = "/hook1";
    final String HOOK_2 = "/hook2";


    @Before
    public void setUp() throws Exception {
        //start mock web server
        //create hooks
        final String baseServerUrl = webserver(dispatcher());
        log.info("Mock server url: " + baseServerUrl);

        hooks.add(webhookDao.create(hook("partyId1", "http://" + baseServerUrl + HOOK_1, EventType.INVOICE_CREATED)));
        hooks.add(webhookDao.create(hook("partyId1", "http://" + baseServerUrl + HOOK_2, EventType.INVOICE_CREATED, EventType.INVOICE_PAYMENT_STARTED)));
    }


    @Test
    public void testMessageSend() throws InterruptedException {
        final Message sourceMessage = messageDao.create(message("1", "partyId1", EventType.INVOICE_CREATED, "status"));

        Message receivedMessage1 = hook1Queue.poll(1, TimeUnit.SECONDS);
        Message receivedMessage2 = hook2Queue.poll(1, TimeUnit.SECONDS);

        assertEquals(sourceMessage.getInvoiceId(), receivedMessage1.getInvoiceId());
        assertEquals(sourceMessage.getInvoiceId(), receivedMessage2.getInvoiceId());

        assertTrue(hook1Queue.isEmpty());
        assertTrue(hook2Queue.isEmpty());

    }

    private static Message message(String invoceId, String partyId, EventType type, String status){
        Message message = new Message();
        message.setEventId(5555);
        message.setInvoiceId(invoceId);
        message.setPartyId(partyId);
        message.setShopId(123);
        message.setAmount(12235);
        message.setCurrency("RUB");
        message.setCreatedAt("12.12.2008");
        Content metadata = new Content();
        metadata.setType("string");
        metadata.setData("somedata".getBytes());
        message.setMetadata(metadata);
        message.setEventType(type);
        message.setType("invoice");
        message.setStatus(status);
        message.setPaymentId("paymentId");
        return message;
    }

    private static Hook hook(String partyId, String url, EventType... types){
        Hook hook = new Hook();
        hook.setPartyId(partyId);
        hook.setUrl(url);

        Set<WebhookAdditionalFilter> webhookAdditionalFilters = new HashSet<>();
        for(EventType type: types){
            webhookAdditionalFilters.add(new WebhookAdditionalFilter(type));
        }
        hook.setFilters(webhookAdditionalFilters);

        return hook;
    }

    private Dispatcher dispatcher(){
        final Dispatcher dispatcher = new Dispatcher() {

            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().startsWith(HOOK_1)) {
                    hook1Queue.put(extract(request));
                    return new MockResponse().setBody(HOOK_1).setResponseCode(200);
                }
                if (request.getPath().startsWith(HOOK_2)) {
                    hook2Queue.put(extract(request));
                    return new MockResponse().setBody(HOOK_2).setResponseCode(200);
                }
                return new MockResponse().setResponseCode(500);
            }
        };
        return dispatcher;
    }

    private String webserver(Dispatcher dispatcher)  {
        final MockWebServer server = new MockWebServer();
        server.setDispatcher(dispatcher);
        try {
            server.start();
        } catch (IOException e) {
            throw  new RuntimeException(e);
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
    private static Message extract(RecordedRequest request) {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            request.getBody().writeTo(bout);
            return  new ObjectMapper().readValue(bout.toByteArray(), Message.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
