package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.dao.impl.InvoicingQueueDao;
import com.rbkmoney.hooker.dao.impl.InvoicingTaskDao;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.InvoicingMessageEnum;
import com.rbkmoney.hooker.service.BatchService;
import com.rbkmoney.hooker.utils.BuildUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;

import static com.rbkmoney.hooker.utils.BuildUtils.cart;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HookDeleteDaoTest extends AbstractIntegrationTest {
    @Autowired
    InvoicingTaskDao taskDao;

    @Autowired
    InvoicingQueueDao queueDao;

    @Autowired
    HookDao hookDao;

    @Autowired
    BatchService batchService;

    @Test
    public void setUp() {
        Long hookId = hookDao.create(HookDaoImplTest.buildHook("partyId", "fake.url")).getId();
        Long hookId2 = hookDao.create(HookDaoImplTest.buildHook("partyId2", "fake2.url")).getId();
        batchService.process(Collections.singletonList(BuildUtils.buildMessage(InvoicingMessageEnum.INVOICE.value(),"2345", "partyId", EventType.INVOICE_CREATED, "status", cart(), true)));
        assertEquals(queueDao.getWithPolicies(taskDao.getScheduled(new ArrayList<>()).keySet()).size(), 1);
        hookDao.delete(hookId2);
        assertNotEquals(queueDao.getWithPolicies(taskDao.getScheduled(new ArrayList<>()).keySet()).size(), 0);
        hookDao.delete(hookId);
        assertEquals(taskDao.getScheduled(new ArrayList<>()).keySet().size(), 0);
    }
}
