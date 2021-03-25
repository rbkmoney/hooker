package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.dao.impl.InvoicingMessageDaoImpl;
import com.rbkmoney.hooker.exception.NotFoundException;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.InvoiceStatusEnum;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.InvoicingMessageEnum;
import com.rbkmoney.hooker.model.InvoicingMessageKey;
import com.rbkmoney.hooker.model.PaymentStatusEnum;
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
import java.util.List;

import static com.rbkmoney.hooker.utils.BuildUtils.buildMessage;
import static org.junit.Assert.assertEquals;

/**
 * Created by inalarsanukaev on 09.04.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InvoicingMessageDaoImplTest extends AbstractIntegrationTest {
    private static Logger log = LoggerFactory.getLogger(InvoicingMessageDaoImplTest.class);
    private static boolean messagesCreated = false;
    @Autowired
    InvoicingMessageDaoImpl messageDao;

    @Before
    public void setUp() throws Exception {
        if (!messagesCreated) {
            messageDao.saveBatch(Arrays.asList(
                    buildMessage(InvoicingMessageEnum.INVOICE.getValue(), "1234", "56678", EventType.INVOICE_CREATED,
                            InvoiceStatusEnum.PAID, PaymentStatusEnum.CAPTURED),
                    buildMessage(InvoicingMessageEnum.INVOICE.getValue(), "1235", "56678", EventType.INVOICE_CREATED,
                            InvoiceStatusEnum.PAID, PaymentStatusEnum.CAPTURED),
                    buildMessage(InvoicingMessageEnum.PAYMENT.getValue(), "1236", "56678", EventType.INVOICE_CREATED,
                            InvoiceStatusEnum.PAID, PaymentStatusEnum.CAPTURED)));
            messagesCreated = true;
        }
    }

    @Test
    public void get() throws Exception {
        InvoicingMessage message = messageDao.getInvoicingMessage(
                InvoicingMessageKey.builder().invoiceId("1235").type(InvoicingMessageEnum.INVOICE).build());
        assertEquals(message.getInvoiceId(), "1235");
        assertEquals(message.getInvoiceStatus(), InvoiceStatusEnum.PAID);

        List<InvoicingMessage> messages = messageDao.getBy(Arrays.asList(message.getId()));
        assertEquals(1, messages.size());
        assertEquals(messages.get(0).getPartyId(), "56678");

        InvoicingMessage payment = messageDao.getInvoicingMessage(
                InvoicingMessageKey.builder().invoiceId("1236").paymentId("123").type(InvoicingMessageEnum.PAYMENT)
                        .build());
        assertEquals("123", payment.getPaymentId());
    }

    @Ignore
    @Test(expected = NotFoundException.class)
    public void testNotFound() {
        messageDao.getInvoicingMessage(
                InvoicingMessageKey.builder().invoiceId("kek").paymentId("lol").refundId("kk").build());
    }
}
