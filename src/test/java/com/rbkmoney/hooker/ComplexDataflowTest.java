package com.rbkmoney.hooker;

import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.dao.WebhookAdditionalFilter;
import com.rbkmoney.hooker.dao.impl.InvoicingQueueDao;
import com.rbkmoney.hooker.dao.impl.InvoicingTaskDao;
import com.rbkmoney.hooker.model.*;
import com.rbkmoney.hooker.model.Queue;
import com.rbkmoney.hooker.retry.impl.simple.SimpleRetryPolicyRecord;
import com.rbkmoney.hooker.service.BatchService;
import com.rbkmoney.hooker.utils.KeyUtils;
import com.rbkmoney.swag_webhook_events.model.Event;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rbkmoney.hooker.utils.BuildUtils.buildMessage;
import static org.junit.Assert.*;

/**
 * Created by jeckep on 20.04.17.
 */
@TestPropertySource(properties = {"message.scheduler.delay=500"})
@Slf4j
public class ComplexDataflowTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HookDao hookDao;

    @Autowired
    private BatchService batchService;

    @Autowired
    private InvoicingQueueDao queueDao;

    @Autowired
    private InvoicingMessageDao invoicingMessageDao;

    @Autowired
    private InvoicingTaskDao taskDao;

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
            WebhookAdditionalFilter w = WebhookAdditionalFilter.builder().eventType(EventType.INVOICE_STATUS_CHANGED).build();
            w.setInvoiceStatus("unpaid");
            wSet.add(w);
            w = WebhookAdditionalFilter.builder().eventType(EventType.INVOICE_PAYMENT_STATUS_CHANGED).build();
            w.setInvoicePaymentStatus("captured");
            wSet.add(w);
            hooks.add(hookDao.create(hookPaymentStatus("partyId1", "http://" + baseServerUrl + HOOK_1, wSet)));

            wSet.clear();
            w = WebhookAdditionalFilter.builder().eventType(EventType.INVOICE_PAYMENT_STATUS_CHANGED).build();
            w.setInvoicePaymentStatus("failed");
            wSet.add(w);
            hooks.add(hookDao.create(hookPaymentStatus("partyId1", "http://" + baseServerUrl + HOOK_2, wSet)));
        }
    }

    @Test
    public void testMessageSend() throws InterruptedException {
        List<InvoicingMessage> sourceMessages = new ArrayList<>();
        sourceMessages.addAll(Arrays.asList(
                buildMessage(InvoicingMessageEnum.INVOICE.value(),"1", "partyId1", EventType.INVOICE_STATUS_CHANGED, "unpaid", null, true, 0L, 0),
                buildMessage(InvoicingMessageEnum.PAYMENT.value(), "1", "partyId1", EventType.INVOICE_PAYMENT_STATUS_CHANGED, "captured", null, true, 0L, 1),
                buildMessage(InvoicingMessageEnum.PAYMENT.value(), "2", "partyId1", EventType.INVOICE_PAYMENT_STATUS_CHANGED, "processed", null, true, 0L, 0),
                buildMessage(InvoicingMessageEnum.PAYMENT.value(), "2", "partyId1", EventType.INVOICE_PAYMENT_STATUS_CHANGED, "failed", null, true, 0L, 1),
                buildMessage(InvoicingMessageEnum.PAYMENT.value(), "3", "partyId1", EventType.INVOICE_PAYMENT_STATUS_CHANGED, "failed", null, true, 0L, 1)));

        batchService.process(sourceMessages);

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

        InvoicingMessage invoicingMessageFailed = invoicingMessageDao.getInvoicingMessage(InvoicingMessageKey.builder().invoiceId("3").paymentId("123").type(InvoicingMessageEnum.PAYMENT).build());
        assertNotNull(invoicingMessageFailed);
        assertEquals(sourceMessages.get(sourceMessages.size() - 1).getInvoice().getAmount(), invoicingMessageFailed.getInvoice().getAmount());

        Thread.currentThread().sleep(500);

        assertTrue(taskDao.getScheduled().isEmpty());
        assertEquals(1, jdbcTemplate.queryForList("select * from hook.scheduled_task").size());
        assertEquals(6, jdbcTemplate.queryForList("select * from hook.simple_retry_policy").size());

        Long queueIdFailed = jdbcTemplate.queryForObject("select id from hook.invoicing_queue order by id desc limit 1", Long.class);

        List<InvoicingQueue> policies = queueDao.getWithPolicies(Collections.singletonList(queueIdFailed));
        assertEquals(1, policies.size());
        assertTrue(policies.get(0).getRetryPolicyRecord().isFailed());
        SimpleRetryPolicyRecord retryPolicyRecord = (SimpleRetryPolicyRecord) policies.get(0).getRetryPolicyRecord();
        assertEquals(1, retryPolicyRecord.getFailCount().longValue());
        assertNotNull(retryPolicyRecord.getLastFailTime());
        assertNotNull(retryPolicyRecord.getLastFailTime());
        assertNotNull(retryPolicyRecord.getNextFireTime());
        assertEquals(30 * 1000, retryPolicyRecord.getNextFireTime() - retryPolicyRecord.getLastFailTime());

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
                    return new MockResponse().setBody("fail").setResponseCode(500);
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
