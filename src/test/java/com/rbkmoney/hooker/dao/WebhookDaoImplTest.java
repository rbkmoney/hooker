package com.rbkmoney.hooker.dao;

import com.rbkmoney.damsel.webhooker.Webhook;
import com.rbkmoney.damsel.webhooker.WebhookParams;
import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.utils.EventFilterUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by inalarsanukaev on 08.04.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebhookDaoImplTest extends AbstractIntegrationTest {
    @Autowired
    WebhookDao webhookDao;
    @Before
    public void setUp() throws Exception {
        Set<EventTypeCode> eventTypeCodeSet = new HashSet<>();
        eventTypeCodeSet.add(EventTypeCode.INVOICE_PAYMENT_STATUS_CHANGED);
        eventTypeCodeSet.add(EventTypeCode.INVOICE_CREATED);
        WebhookParams webhookParams = new WebhookParams("123", EventFilterUtils.getEventFilterByCode(eventTypeCodeSet), "https://google.com");
        webhookDao.addWebhook(webhookParams);
        eventTypeCodeSet.clear();
        eventTypeCodeSet.add(EventTypeCode.INVOICE_STATUS_CHANGED);
        eventTypeCodeSet.add(EventTypeCode.INVOICE_PAYMENT_STARTED);
        webhookParams = new WebhookParams("999", EventFilterUtils.getEventFilterByCode(eventTypeCodeSet), "https://yandex.ru");
        webhookDao.addWebhook(webhookParams);
        eventTypeCodeSet.clear();
        eventTypeCodeSet.add(EventTypeCode.INVOICE_STATUS_CHANGED);
        webhookParams = new WebhookParams("123", EventFilterUtils.getEventFilterByCode(eventTypeCodeSet), "https://2ch.hk/b");
        webhookDao.addWebhook(webhookParams);
    }

    @After
    public void tearDown() throws Exception {
        List<Webhook> list = webhookDao.getPartyWebhooks("123");
        for (Webhook w : list) {
            webhookDao.delete(w.getId());
        }
        list = webhookDao.getPartyWebhooks("999");
        for (Webhook w : list) {
            webhookDao.delete(w.getId());
        }
    }

    @Test
    public void getPartyWebhooks() throws Exception {
        Assert.assertEquals(webhookDao.getPartyWebhooks("123").size(), 2);
        Assert.assertTrue(webhookDao.getPartyWebhooks("88888").isEmpty());
    }

    @Test
    public void getWebhookById() throws Exception {
        List<Webhook> list = webhookDao.getPartyWebhooks("123");
        for (Webhook w : list) {
            System.out.println(w);
            Assert.assertNotNull(webhookDao.getWebhookById(w.getId()));
        }
    }

    @Test
    public void getWebhooksByCode() throws Exception {
//        Assert.assertNotNull(webhookDao.getWebhooksByCode(EventTypeCode.INVOICE_CREATED, "123"));
        Assert.assertTrue(webhookDao.getWebhooksByCode(EventTypeCode.INVOICE_CREATED, "888").isEmpty());
    }

    @Test
    public void getPairKey() throws Exception {
        Assert.assertNotNull(webhookDao.getPairKey("123"));
        Assert.assertNull(webhookDao.getPairKey("88888"));
    }
}
