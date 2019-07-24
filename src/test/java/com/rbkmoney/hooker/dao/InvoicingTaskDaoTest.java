package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.dao.impl.InvoicingQueueDao;
import com.rbkmoney.hooker.dao.impl.InvoicingTaskDao;
import com.rbkmoney.hooker.handler.poller.impl.invoicing.AbstractInvoiceEventHandler;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Task;
import com.rbkmoney.hooker.utils.BuildUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.stream.Collectors;

import static com.rbkmoney.hooker.utils.BuildUtils.cart;
import static org.junit.Assert.*;

/**
 * Created by jeckep on 17.04.17.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InvoicingTaskDaoTest extends AbstractIntegrationTest {
    @Autowired
    InvoicingTaskDao taskDao;

    @Autowired
    InvoicingQueueDao queueDao;

    @Autowired
    HookDao hookDao;

    @Autowired
    InvoicingMessageDao messageDao;

    @Autowired
    private TransactionTemplate transactionTemplate;

    Long messageId;
    Long hookId;

    @Before
    public void setUp() throws Exception {
        hookId = hookDao.create(HookDaoImplTest.buildHook("partyId", "fake.url")).getId();
        messageDao.create(BuildUtils.buildMessage(AbstractInvoiceEventHandler.INVOICE,"2345", "partyId", EventType.INVOICE_CREATED, "status", cart(), true));
        messageId = messageDao.getInvoice("2345").getId();
    }

    @After
    public void after() throws Exception {
        hookDao.delete(hookId);
    }

    @Test
    public void createDeleteGet() {
        Map<Long, List<Task>> scheduled = taskDao.getScheduled(new ArrayList<>());
        assertEquals(1, scheduled.size());
        taskDao.remove(scheduled.keySet().iterator().next(), messageId);
        assertEquals(0, taskDao.getScheduled(new ArrayList<>()).size());
    }

    @Test
    public void testSelectForUpdate() {
        for (int i = 0; i < 20; ++i) {
            messageDao.create(BuildUtils.buildMessage(AbstractInvoiceEventHandler.INVOICE, ""+i, "partyId", EventType.INVOICE_CREATED, "status", cart(), true));
        }

        Set<Long> scheduledOne = new HashSet<>();
        new Thread(() -> transactionTemplate.execute(tr -> {
            scheduledOne.addAll(taskDao.getScheduled(new ArrayList<>()).values().stream().flatMap(List::stream).map(Task::getMessageId).collect(Collectors.toSet()));
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

        Set<Long> scheduledTwo = new HashSet<>();
        new Thread(() -> transactionTemplate.execute(tr -> {
            scheduledTwo.addAll(taskDao.getScheduled(new ArrayList<>()).values().stream().flatMap(List::stream).map(Task::getMessageId).collect(Collectors.toSet()));
            System.out.println("scheduledTwo :" + scheduledTwo);
            return 1;
        })).start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scheduledOne.retainAll(scheduledTwo);
        assertTrue(scheduledOne.isEmpty());
    }

    @Test
    public void removeAll() {
        taskDao.removeAll(hookId);
    }
}
