package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.dao.impl.InvoicingMessageDaoImpl;
import com.rbkmoney.hooker.dao.impl.InvoicingQueueDao;
import com.rbkmoney.hooker.dao.impl.InvoicingTaskDao;
import com.rbkmoney.hooker.model.*;
import com.rbkmoney.hooker.utils.BuildUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Created by jeckep on 17.04.17.
 */

@TestPropertySource(properties = "message.scheduler.invoicing.threadPoolSize=0")
public class InvoicingTaskDaoTest extends AbstractIntegrationTest {

    @Autowired
    InvoicingTaskDao taskDao;

    @Autowired
    InvoicingQueueDao queueDao;

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    HookDao hookDao;

    @Autowired
    InvoicingMessageDaoImpl messageDao;

    @Autowired
    private TransactionTemplate transactionTemplate;

    Long messageId;
    Long hookId;

    @Before
    public void setUp() throws Exception {
        hookId = hookDao.create(HookDaoImplTest.buildHook("partyId", "fake.url")).getId();
    }

    @After
    public void after() throws Exception {
        jdbcTemplate.update("truncate hook.scheduled_task, hook.invoicing_queue, hook.message, hook.webhook_to_events, hook.webhook", new HashMap<>());
    }

    @Test
    public void createDeleteGet() {
        messageDao.saveBatch(Collections.singletonList(BuildUtils.buildMessage(InvoicingMessageEnum.INVOICE.getValue(),"2345", "partyId", EventType.INVOICE_CREATED, InvoiceStatusEnum.PAID, PaymentStatusEnum.CAPTURED)));
        messageId = messageDao.getInvoicingMessage(InvoicingMessageKey.builder().invoiceId("2345").type(InvoicingMessageEnum.INVOICE).build()).getId();
        queueDao.saveBatchWithPolicies(Collections.singletonList(messageId));
        taskDao.save(Collections.singletonList(messageId));
        Map<Long, List<Task>> scheduled = taskDao.getScheduled();
        assertEquals(1, scheduled.size());
        taskDao.remove(scheduled.keySet().iterator().next(), messageId);
        assertEquals(0, taskDao.getScheduled().size());
    }

    @Test
    public void testSaveWithHookIdAndInvoiceId(){
        messageDao.saveBatch(Collections.singletonList(BuildUtils.buildMessage(InvoicingMessageEnum.INVOICE.getValue(),"2345", "partyId", EventType.INVOICE_CREATED, InvoiceStatusEnum.PAID, PaymentStatusEnum.CAPTURED)));
        messageId = messageDao.getInvoicingMessage(InvoicingMessageKey.builder().invoiceId("2345").type(InvoicingMessageEnum.INVOICE).build()).getId();
        queueDao.saveBatchWithPolicies(Collections.singletonList(messageId));
        int count = taskDao.save(hookId, "2345");
        assertEquals(1, count);
    }

    @Test
    public void testSelectForUpdate() {

        int cnt = 20;

        List<InvoicingMessage> messagesOne = IntStream.range(0, cnt).mapToObj(i -> BuildUtils.buildMessage(InvoicingMessageEnum.INVOICE.getValue(), "invoice_id1", "partyId", EventType.INVOICE_CREATED, InvoiceStatusEnum.PAID, PaymentStatusEnum.CAPTURED))
                .collect(Collectors.toList());

        List<InvoicingMessage> messagesSecond = IntStream.range(0, cnt).mapToObj(i -> BuildUtils.buildMessage(InvoicingMessageEnum.INVOICE.getValue(), "invoice_id2", "partyId", EventType.INVOICE_CREATED, InvoiceStatusEnum.PAID, PaymentStatusEnum.CAPTURED))
                .collect(Collectors.toList());

        messageDao.saveBatch(messagesOne);
        List<Long> messageIdsOne = messagesOne.stream().map(Message::getId).collect(Collectors.toList());
        queueDao.saveBatchWithPolicies(messageIdsOne);
        taskDao.save(messageIdsOne);

        messageDao.saveBatch(messagesSecond);
        List<Long> messageIdsSecond = messagesSecond.stream().map(Message::getId).collect(Collectors.toList());
        queueDao.saveBatchWithPolicies(messageIdsSecond);
        taskDao.save(messageIdsSecond);

        Set<Long> scheduledOne = new HashSet<>();
        new Thread(() -> transactionTemplate.execute(tr -> {
            scheduledOne.addAll(taskDao.getScheduled().values().stream().flatMap(List::stream).map(Task::getMessageId).collect(Collectors.toSet()));
            System.out.println("scheduledOne: " + scheduledOne);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 1;
        })).start();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(cnt, scheduledOne.size());

        Set<Long> scheduledTwo = new HashSet<>();
        new Thread(() -> transactionTemplate.execute(tr -> {
            scheduledTwo.addAll(taskDao.getScheduled().values().stream().flatMap(List::stream).map(Task::getMessageId).collect(Collectors.toSet()));
            System.out.println("scheduledTwo :" + scheduledTwo);
            return 1;
        })).start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(cnt, scheduledTwo.size());

        scheduledOne.retainAll(scheduledTwo);
        assertTrue(scheduledOne.isEmpty());
    }

    @Test
    public void testSelectForUpdateWithLockQueue() {

        hookDao.create(HookDaoImplTest.buildHook("partyId", "fake2.url"));

        InvoicingMessage message = BuildUtils.buildMessage(InvoicingMessageEnum.INVOICE.getValue(), "1", "partyId", EventType.INVOICE_CREATED, InvoiceStatusEnum.PAID, PaymentStatusEnum.CAPTURED);

        messageDao.saveBatch(List.of(message));
        queueDao.saveBatchWithPolicies(List.of(message.getId()));
        taskDao.save(List.of(message.getId()));

        Set<String> scheduledOne = new HashSet<>();
        new Thread(() -> transactionTemplate.execute(tr -> {
            scheduledOne.addAll(taskDao.getScheduled().values().stream().flatMap(List::stream).map(t -> t.getMessageId() + " " + t.getQueueId()).collect(Collectors.toSet()));
            System.out.println("scheduledOne: " + scheduledOne);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 1;
        })).start();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(2, scheduledOne.size());

        Set<String> scheduledTwo = new HashSet<>();
        new Thread(() -> transactionTemplate.execute(tr -> {
            scheduledTwo.addAll(taskDao.getScheduled().values().stream().flatMap(List::stream).map(t -> t.getMessageId() + " " + t.getQueueId()).collect(Collectors.toSet()));
            System.out.println("scheduledTwo :" + scheduledTwo);
            return 1;
        })).start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(scheduledTwo.isEmpty());
    }


    @Test
    public void removeAll() {
        taskDao.removeAll(hookId);
    }
}
