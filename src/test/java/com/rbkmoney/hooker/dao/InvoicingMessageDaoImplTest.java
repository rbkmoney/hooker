package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.handler.poller.impl.invoicing.AbstractInvoiceEventHandler;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.swag_webhook_events.model.CustomerPayer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static com.rbkmoney.hooker.utils.BuildUtils.buildMessage;
import static com.rbkmoney.hooker.utils.BuildUtils.cart;
import static org.junit.Assert.*;

/**
 * Created by inalarsanukaev on 09.04.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InvoicingMessageDaoImplTest extends AbstractIntegrationTest {
    private static Logger log = LoggerFactory.getLogger(InvoicingMessageDaoImplTest.class);

    @Autowired
    InvoicingMessageDao messageDao;

    private static boolean messagesCreated = false;

    @Before
    public void setUp() throws Exception {
        if(!messagesCreated){
            messageDao.create(buildMessage(AbstractInvoiceEventHandler.INVOICE,"1234", "56678", EventType.INVOICE_CREATED, "status"));
            messageDao.create(buildMessage(AbstractInvoiceEventHandler.INVOICE,"1235", "56678", EventType.INVOICE_CREATED, "status", cart(), true));
            messageDao.create(buildMessage(AbstractInvoiceEventHandler.PAYMENT,"1236", "56678", EventType.INVOICE_CREATED, "status", cart(), false));
            messagesCreated = true;
        }
    }

    @Test
    public void testGetAny() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            messageDao.getInvoice("1234");
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
        InvoicingMessage message = messageDao.getInvoice("1235");
        assertTrue(message.getEventId() >= 380000000);
        assertEquals(message.getInvoice().getAmount(), 12235);
        assertEquals(message.getInvoice().getCart().size(), 2);

        List<InvoicingMessage> messages = messageDao.getBy(Arrays.asList(message.getId()));
        assertEquals(1, messages.size());
        assertFalse(messages.get(0).getInvoice().getCart().isEmpty());

        InvoicingMessage payment = messageDao.getPayment("1236", "123");
        assertTrue(payment.getPayment().getPayer() instanceof CustomerPayer);
    }

    @Test
    public void testDuplication(){
        InvoicingMessage message = buildMessage(AbstractInvoiceEventHandler.INVOICE, "1234", "56678", EventType.INVOICE_CREATED, "status");
        messageDao.create(message);
        assertNull(message.getId());

    }

    @Test(expected = NotFoundException.class)
    public void testNotFound(){
        messageDao.getRefund("kek", "lol", "kk");
    }
}
