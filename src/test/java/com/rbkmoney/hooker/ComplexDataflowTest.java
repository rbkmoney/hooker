package com.rbkmoney.hooker;

import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.dao.SimpleRetryPolicyDao;
import com.rbkmoney.hooker.dao.WebhookAdditionalFilter;
import com.rbkmoney.hooker.handler.poller.impl.invoicing.AbstractInvoiceEventHandler;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.model.Message;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static com.rbkmoney.hooker.utils.BuildUtils.message;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jeckep on 20.04.17.
 */
@TestPropertySource(properties = {"message.scheduler.delay=100"})
public class ComplexDataflowTest extends AbstractIntegrationTest {
    private static Logger log = LoggerFactory.getLogger(ComplexDataflowTest.class);

    @Autowired
    HookDao hookDao;

    @Autowired
    MessageDao messageDao;

    @Autowired
    SimpleRetryPolicyDao simpleRetryPolicyDao;

    BlockingQueue<DataflowTest.MockMessage> hook1Queue = new LinkedBlockingDeque<>(10);
    BlockingQueue<DataflowTest.MockMessage> hook2Queue = new LinkedBlockingDeque<>(10);

    final List<Hook> hooks = new ArrayList<>();
    final String HOOK_1 = "/hook1";
    final String HOOK_2 = "/hook2";

    String baseServerUrl;


    @Before
    public void setUp() throws Exception {
        //start mock web server
        //create hooks
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
        List<Message> sourceMessages = new ArrayList<>();
        sourceMessages.add(messageDao.create(message(AbstractInvoiceEventHandler.INVOICE,"1", "partyId1", EventType.INVOICE_STATUS_CHANGED, "unpaid")));
        sourceMessages.add(messageDao.create(message(AbstractInvoiceEventHandler.PAYMENT,"1", "partyId1", EventType.INVOICE_PAYMENT_STATUS_CHANGED, "captured")));
        sourceMessages.add(messageDao.create(message(AbstractInvoiceEventHandler.PAYMENT,"1", "partyId1", EventType.INVOICE_PAYMENT_STATUS_CHANGED, "processed")));
        sourceMessages.add(messageDao.create(message(AbstractInvoiceEventHandler.PAYMENT,"1", "partyId1", EventType.INVOICE_PAYMENT_STATUS_CHANGED, "failed")));

        List<DataflowTest.MockMessage> hooks = new ArrayList<>();

        hooks.add(hook1Queue.poll(1, TimeUnit.SECONDS));
        hooks.add(hook1Queue.poll(1, TimeUnit.SECONDS));
        hooks.add(hook2Queue.poll(1, TimeUnit.SECONDS));

        Assert.assertNotNull(hooks.get(0));
        Assert.assertNotNull(hooks.get(1));
        assertEquals(sourceMessages.get(0).getInvoice().getStatus(), hooks.get(0).getInvoice().getStatus());
        assertEquals(sourceMessages.get(1).getPayment().getStatus(), hooks.get(1).getPayment().getStatus());
        assertEquals(sourceMessages.get(3).getPayment().getStatus(), hooks.get(2).getPayment().getStatus());


        assertTrue(hook1Queue.isEmpty());
        assertTrue(hook2Queue.isEmpty());

        Thread.currentThread().sleep(1000);

    }

    private static Hook hookPaymentStatus(String partyId, String url, Set<WebhookAdditionalFilter> webhookAdditionalFilters) {
        Hook hook = new Hook();
        hook.setPartyId(partyId);
        hook.setUrl(url);
        hook.setFilters(webhookAdditionalFilters);
        return hook;
    }

    private Dispatcher dispatcher() {
        final Dispatcher dispatcher = new Dispatcher() {

            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().startsWith(HOOK_1)) {
                    hook1Queue.put(DataflowTest.extractPaymentResourcePayer(request));
                    Thread.sleep(100);
                    return new MockResponse().setBody(HOOK_1).setResponseCode(200);
                }
                if (request.getPath().startsWith(HOOK_2)) {
                    hook2Queue.put(DataflowTest.extractPaymentResourcePayer(request));
                    Thread.sleep(100);
                    return new MockResponse().setBody(HOOK_2).setResponseCode(200);
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
}
