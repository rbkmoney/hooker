package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.dao.impl.InvoicingTaskDao;
import com.rbkmoney.hooker.handler.poller.impl.invoicing.AbstractInvoiceEventHandler;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.utils.BuildUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.rbkmoney.hooker.utils.BuildUtils.cart;
import static org.junit.Assert.assertEquals;

/**
 * Created by jeckep on 17.04.17.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InvoicingTaskDaoTest extends AbstractIntegrationTest {
    @Autowired
    InvoicingTaskDao taskDao;

    @Autowired
    HookDao hookDao;

    @Autowired
    MessageDao messageDao;

    Long messageId;
    Long hookId;

    @Before
    public void setUp() throws Exception {
        hookId = hookDao.create(HookDaoImplTest.buildHook("partyId", "fake.url")).getId();
        messageDao.create(BuildUtils.message(AbstractInvoiceEventHandler.INVOICE,"2345", "partyId", EventType.INVOICE_CREATED, "status", cart(), true));
        messageId = messageDao.getAny("2345", AbstractInvoiceEventHandler.INVOICE).getId();
    }

    @After
    public void after() throws Exception {
        hookDao.delete(hookId);
    }

    @Test
    public void createDeleteGet() {
        assertEquals(1, taskDao.getAll().size());
        taskDao.remove(hookId, messageId);
        assertEquals(0, taskDao.getAll().size());

    }

    @Test
    public void removeAll() {
        assertEquals(1, taskDao.getAll().size());
        taskDao.removeAll(hookId);
        assertEquals(0, taskDao.getAll().size());
    }
}
