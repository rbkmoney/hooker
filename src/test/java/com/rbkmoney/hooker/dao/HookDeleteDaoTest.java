package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.dao.impl.*;
import com.rbkmoney.hooker.model.*;
import com.rbkmoney.hooker.service.BatchService;
import com.rbkmoney.hooker.utils.BuildUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static com.rbkmoney.hooker.utils.BuildUtils.buildCustomerMessage;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HookDeleteDaoTest extends AbstractIntegrationTest {
    @Value("${message.scheduler.limit}")
    private int limit;

    @Autowired
    InvoicingTaskDao taskDao;

    @Autowired
    CustomerTaskDao customerTaskDao;

    @Autowired
    InvoicingQueueDao queueDao;

    @Autowired
    CustomerQueueDao customerQueueDao;

    @Autowired
    HookDao hookDao;

    @Autowired
    CustomerDaoImpl customerDaoImpl;

    @Autowired
    BatchService batchService;

    @Test
    public void testDelete() {
        Long hookId = hookDao.create(HookDaoImplTest.buildHook("partyId", "fake.url")).getId();
        Long hookId2 = hookDao.create(HookDaoImplTest.buildHook("partyId2", "fake2.url")).getId();
        batchService.process(Collections.singletonList(BuildUtils.buildMessage(InvoicingMessageEnum.INVOICE.getValue(),"2345", "partyId", EventType.INVOICE_CREATED, InvoiceStatusEnum.FULFILLED, PaymentStatusEnum.CAPTURED)));
        assertEquals(queueDao.getWithPolicies(taskDao.getScheduled(limit).keySet()).size(), 1);
        hookDao.delete(hookId2);
        assertNotEquals(queueDao.getWithPolicies(taskDao.getScheduled(limit).keySet()).size(), 0);
        hookDao.delete(hookId);
        assertTrue(taskDao.getScheduled(limit).keySet().isEmpty());
        assertFalse(hookDao.getHookById(hookId).isEnabled());
        assertFalse(hookDao.getHookById(hookId2).isEnabled());
    }

    @Test
    public void testDeleteCustomerHooks() {
        Long hookId = hookDao.create(HookDaoImplTest.buildCustomerHook("partyId", "fake.url")).getId();
        Long messageId = customerDaoImpl.create(buildCustomerMessage(1L, "partyId", EventType.CUSTOMER_CREATED, CustomerMessageEnum.CUSTOMER, "124", "4356"));
        customerQueueDao.createWithPolicy(messageId);
        customerTaskDao.create(messageId);
        assertEquals(customerQueueDao.getWithPolicies(customerTaskDao.getScheduled(limit).keySet()).size(), 1);
        hookDao.delete(hookId);
        assertTrue(customerTaskDao.getScheduled(limit).keySet().isEmpty());
        assertFalse(hookDao.getHookById(hookId).isEnabled());
    }
}
