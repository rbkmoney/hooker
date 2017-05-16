package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.handler.poller.impl.AbstractInvoiceEventHandler;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static com.rbkmoney.hooker.utils.BuildUtils.message;
import static org.junit.Assert.assertEquals;

/**
 * Created by inalarsanukaev on 09.04.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MessageDaoImplTest extends AbstractIntegrationTest {
    private static Logger log = LoggerFactory.getLogger(MessageDaoImplTest.class);

    @Autowired
    MessageDao messageDao;

    private static boolean messagesCreated = false;

    @Before
    public void setUp() throws Exception {
        if(!messagesCreated){
            messageDao.create(message(AbstractInvoiceEventHandler.INVOICE,"1234", "56678", EventType.INVOICE_CREATED, "status"));
            messageDao.create(message(AbstractInvoiceEventHandler.INVOICE,"1234", "56678", EventType.INVOICE_CREATED, "status"));
            messagesCreated = true;
        }
    }

    @Test
    public void testGetAny() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            messageDao.getAny("1234");
        }

        long executionTime = System.currentTimeMillis() - startTime;
        if (executionTime > 1000) {
            log.error("Execution time: " + executionTime + ".Seems caching not working!!!");
        } else {
            log.info("Execution time: " + executionTime);
        }


    }

    @Test
    public void get() throws Exception {
        Message message = messageDao.getAny("1234");
        assertEquals(message.getInvoice().getAmount(), 12235);

        assertEquals(1, messageDao.getBy(Arrays.asList(message.getId())).size());
    }

    @Test
    public void getMaxEventId() {
        assertEquals(messageDao.getMaxEventId().longValue(), 5555);
    }
}
