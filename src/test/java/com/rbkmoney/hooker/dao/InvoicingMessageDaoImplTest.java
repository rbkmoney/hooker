package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.dao.impl.InvoicingMessageDaoImpl;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.InvoicingMessageEnum;
import com.rbkmoney.hooker.model.InvoicingMessageKey;
import com.rbkmoney.hooker.utils.BuildUtils;
import com.rbkmoney.swag_webhook_events.model.CustomerPayer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
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
    InvoicingMessageDaoImpl messageDao;

    private static boolean messagesCreated = false;

    @Before
    public void setUp() throws Exception {
        if (!messagesCreated) {
            messageDao.saveBatch(Arrays.asList(
                    buildMessage(InvoicingMessageEnum.INVOICE.value(), "1234", "56678", EventType.INVOICE_CREATED, "status"),
                    buildMessage(InvoicingMessageEnum.INVOICE.value(), "1235", "56678", EventType.INVOICE_CREATED, "status", cart(), true),
                    buildMessage(InvoicingMessageEnum.PAYMENT.value(), "1236", "56678", EventType.INVOICE_CREATED, "status", cart(), false)));
            messagesCreated = true;
        }
    }

    @Test
    public void get() throws Exception {
        InvoicingMessage message = messageDao.getInvoicingMessage(InvoicingMessageKey.builder().invoiceId("1235").type(InvoicingMessageEnum.INVOICE).build());
        assertEquals(message.getInvoice().getAmount(), 12235);
        assertEquals(message.getInvoice().getCart().size(), 2);

        List<InvoicingMessage> messages = messageDao.getBy(Arrays.asList(message.getId()));
        assertEquals(1, messages.size());
        assertFalse(messages.get(0).getInvoice().getCart().isEmpty());

        InvoicingMessage payment = messageDao.getInvoicingMessage(InvoicingMessageKey.builder().invoiceId("1236").paymentId("123").type(InvoicingMessageEnum.PAYMENT).build());
        assertTrue(payment.getPayment().getPayer() instanceof CustomerPayer);
    }

    @Ignore
    @Test(expected = NotFoundException.class)
    public void testNotFound(){
        messageDao.getInvoicingMessage(InvoicingMessageKey.builder().invoiceId("kek").paymentId("lol").refundId("kk").build());
    }
}
