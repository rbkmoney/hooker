package com.rbkmoney.hooker;

import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.dao.WebhookAdditionalFilter;
import com.rbkmoney.hooker.handler.poller.impl.invoicing.AbstractInvoiceEventHandler;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.swag_webhook_events.Event;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static com.rbkmoney.hooker.utils.BuildUtils.buildMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jeckep on 20.04.17.
 */
@TestPropertySource(properties = {"message.scheduler.delay=500"})
@Slf4j
public class ComplexDataflowTest extends AbstractIntegrationTest {

    @Autowired
    HookDao hookDao;

    @Autowired
    InvoicingMessageDao messageDao;

    BlockingQueue<DataflowTest.MockMessage> inv1Queue = new LinkedBlockingDeque<>(10);
    BlockingQueue<DataflowTest.MockMessage> inv2Queue = new LinkedBlockingDeque<>(10);

    final List<Hook> hooks = new ArrayList<>();
    final String HOOK_1 = "/hook1";
    final String HOOK_2 = "/hook2";

    String baseServerUrl;


    @Before
    public void setUp() throws Exception {
        //start mock web server
        //createWithPolicy hooks
        if (baseServerUrl == null) {
            baseServerUrl = webserver(dispatcher());
            log.info("Mock server url: " + baseServerUrl);
            Set<WebhookAdditionalFilter> wSet = new HashSet<>();
            WebhookAdditionalFilter w = new WebhookAdditionalFilter(EventType.INVOICE_STATUS_CHANGED);
            w.setInvoiceStatus("unpaid");
            wSet.add(w);
            w = new WebhookAdditionalFilter(EventType.INVOICE_PAYMENT_STATUS_CHANGED);
            w.setInvoicePaymentStatus("captured");
            wSet.add(w);
            hooks.add(hookDao.create(hookPaymentStatus("partyId1", "http://" + baseServerUrl + HOOK_1, wSet)));

            wSet.clear();
            w = new WebhookAdditionalFilter(EventType.INVOICE_PAYMENT_STATUS_CHANGED);
            w.setInvoicePaymentStatus("failed");
            wSet.add(w);
            hooks.add(hookDao.create(hookPaymentStatus("partyId1", "http://" + baseServerUrl + HOOK_2, wSet)));
        }
    }

    @Test
    public void testMessageSend() throws InterruptedException {
        List<InvoicingMessage> sourceMessages = new ArrayList<>();
        InvoicingMessage message = buildMessage(AbstractInvoiceEventHandler.INVOICE,"1", "partyId1", EventType.INVOICE_STATUS_CHANGED, "unpaid", null, true, 0L, 0);
        messageDao.create(message);
        sourceMessages.add(message);
        message = buildMessage(AbstractInvoiceEventHandler.PAYMENT,"1", "partyId1", EventType.INVOICE_PAYMENT_STATUS_CHANGED, "captured", null, true, 0L, 1);
        messageDao.create(message);
        sourceMessages.add(message);
        message = buildMessage(AbstractInvoiceEventHandler.PAYMENT, "2", "partyId1", EventType.INVOICE_PAYMENT_STATUS_CHANGED, "processed", null, true, 0L, 0);
        messageDao.create(message);
        sourceMessages.add(message);
        message = buildMessage(AbstractInvoiceEventHandler.PAYMENT, "2", "partyId1", EventType.INVOICE_PAYMENT_STATUS_CHANGED, "failed", null, true, 0L, 1);
        messageDao.create(message);
        sourceMessages.add(message);

        List<DataflowTest.MockMessage> hooks = new ArrayList<>();

        hooks.add(inv1Queue.poll(1, TimeUnit.SECONDS));
        hooks.add(inv1Queue.poll(1, TimeUnit.SECONDS));
        hooks.add(inv2Queue.poll(1, TimeUnit.SECONDS));

        Assert.assertNotNull(hooks.get(0));
        Assert.assertNotNull(hooks.get(1));
        Assert.assertNotNull(hooks.get(2));
        assertEquals(sourceMessages.get(0).getInvoice().getStatus(), hooks.get(0).getInvoice().getStatus());
        assertEquals(sourceMessages.get(1).getPayment().getStatus(), hooks.get(1).getPayment().getStatus());
        assertEquals(sourceMessages.get(3).getPayment().getStatus(), hooks.get(2).getPayment().getStatus());


        assertTrue(inv1Queue.isEmpty());
        assertTrue(inv2Queue.isEmpty());

        Thread.currentThread().sleep(1000);

    }

    private static Hook hookPaymentStatus(String partyId, String url, Set<WebhookAdditionalFilter> webhookAdditionalFilters) {
        Hook hook = new Hook();
        hook.setPartyId(partyId);
        hook.setUrl(url);
        hook.setFilters(webhookAdditionalFilters);
        hook.setTopic(Event.TopicEnum.INVOICESTOPIC.getValue());
        return hook;
    }

    private Dispatcher dispatcher() {
        return new Dispatcher() {

            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

                DataflowTest.MockMessage mockMessage = DataflowTest.extractPaymentResourcePayer(request);
                String id = mockMessage.getInvoice().getId();
                if (id.equals("1")) {
                    inv1Queue.put(mockMessage);
                } else if (id.equals("2")) {
                    inv2Queue.put(mockMessage);
                } else {
                    Thread.sleep(100);
                    return new MockResponse().setResponseCode(500);
                }
                Thread.sleep(100);
                return new MockResponse().setBody(HOOK_1).setResponseCode(200);
            }
        };
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
}
