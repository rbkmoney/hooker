package com.rbkmoney.hooker.dao;

import com.rbkmoney.damsel.webhooker.EventFilter;
import com.rbkmoney.damsel.webhooker.Webhook;
import com.rbkmoney.damsel.webhooker.WebhookParams;
import com.rbkmoney.hooker.service.crypt.KeyPair;
import com.rbkmoney.hooker.utils.EventFilterUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import java.awt.image.Kernel;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by inalarsanukaev on 08.04.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebhookDaoImplTest {
    @Autowired
    WebhookDao webhookDao;
    @Before
    public void setUp() throws Exception {
        WebhookParams webhookParams = new WebhookParams("123", EventFilterUtils.getEventFilterByCode(EventTypeCode.INVOICE_PAYMENT_STATUS_CHANGED), "https://google.com");
        webhookDao.addWebhook(webhookParams);
        webhookParams = new WebhookParams("123", EventFilterUtils.getEventFilterByCode(EventTypeCode.INVOICE_CREATED), "https://yandex.ru");
        webhookDao.addWebhook(webhookParams);
    }

    @After
    public void tearDown() throws Exception {
        List<Webhook> list = webhookDao.getPartyWebhooks("123");
        for (Webhook w : list) {
            webhookDao.delete(w.getId());
        }
    }

    @Test
    public void getPartyWebhooks() throws Exception {
        Assert.assertEquals(webhookDao.getPartyWebhooks("123").size(), 2);
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
        Assert.assertNotNull(webhookDao.getWebhooksByCode(EventTypeCode.INVOICE_CREATED, "123"));
    }

    @Test
    public void getPairKey() throws Exception {
        Assert.assertNotNull(webhookDao.getPairKey("123"));
    }
}
