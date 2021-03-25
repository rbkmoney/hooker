package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.dao.HookDaoImplTest;
import com.rbkmoney.hooker.model.CustomerMessageEnum;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Task;
import com.rbkmoney.hooker.utils.BuildUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CustomerTaskDaoTest extends AbstractIntegrationTest {

    @Autowired
    private CustomerTaskDao taskDao;

    @Autowired
    private CustomerQueueDao queueDao;

    @Autowired
    private HookDao hookDao;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private CustomerDaoImpl customerDao;

    private Long messageId;
    private Long hookId;
    private String custId = "124";

    @Before
    public void setUp() throws Exception {
        hookId = hookDao.create(HookDaoImplTest.buildCustomerHook("partyId", "fake.url")).getId();
        customerDao.create(BuildUtils
                .buildCustomerMessage(1L, "partyId", EventType.CUSTOMER_CREATED, CustomerMessageEnum.CUSTOMER, custId,
                        "4356"));
        messageId = customerDao.getAny(custId, CustomerMessageEnum.CUSTOMER).getId();
    }

    @After
    public void after() throws Exception {
        jdbcTemplate.update("truncate hook.scheduled_task, hook.customer_queue, hook.customer_message, " +
                "hook.webhook_to_events, hook.webhook", new HashMap<>());
    }

    @Test
    public void createDeleteGet() {
        queueDao.createWithPolicy(messageId);
        taskDao.create(messageId);
        Map<Long, List<Task>> scheduled = taskDao.getScheduled();
        assertEquals(1, scheduled.size());
        taskDao.remove(scheduled.keySet().iterator().next(), messageId);
        assertEquals(0, taskDao.getScheduled().size());
    }

    @Test
    public void testSaveWithHookIdAndCustomerId() {
        queueDao.createWithPolicy(messageId);
        int count = taskDao.create(hookId, custId);
        assertEquals(1, count);
    }

}
