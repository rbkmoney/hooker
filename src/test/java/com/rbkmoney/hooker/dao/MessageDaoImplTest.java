package com.rbkmoney.hooker.dao;

import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.model.EventStatus;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Message;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Created by inalarsanukaev on 09.04.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MessageDaoImplTest extends AbstractIntegrationTest {

    @Autowired
    MessageDao messageDao;

    @Before
    public void setUp() throws Exception {
        messageDao.save(buildMessage("1234", "56678"));
        messageDao.save(buildMessage("1234", "56678"));
    }

    @After
    public void tearDown() throws Exception {
        Assert.assertTrue(messageDao.delete("1234"));
    }

    @Test
    public void get() throws Exception {
        Message message = messageDao.getAny("1234");
        assertEquals(message.getAmount(), 12235);

        assertEquals(1, messageDao.getBy(Arrays.asList(message.getId())).size());
    }

    @Test
    public void getBy() throws Exception {
        List<Message> messages = messageDao.getBy(EventStatus.RECEIVED);
        assertEquals(2, messages.size());
        assertEquals(0, messageDao.getBy(EventStatus.SCHEDULED).size());

        messageDao.updateStatus(messages.stream().map(m -> m.getId()).collect(Collectors.toList()), EventStatus.SCHEDULED);
        assertEquals(2, messageDao.getBy(EventStatus.SCHEDULED).size());

    }

    @Test
    public void getMaxEventId(){
        assertEquals(messageDao.getMaxEventId().longValue(), 5555);
    }

    public static Message buildMessage(String invoceId, String partyId){
        Message message = new Message();
        message.setEventId(5555);
        message.setInvoiceId(invoceId);
        message.setPartyId(partyId);
        message.setShopId(123);
        message.setAmount(12235);
        message.setCurrency("RUB");
        message.setCreatedAt("12.12.2008");
        Content metadata = new Content();
        metadata.setType("string");
        metadata.setData("somedata".getBytes());
        message.setMetadata(metadata);
        message.setEventType(EventType.INVOICE_CREATED);
        message.setEventStatus(EventStatus.RECEIVED);
        message.setType("invoice");
        message.setStatus("message status");
        message.setPaymentId("paymentId");
        return message;
    }
}
