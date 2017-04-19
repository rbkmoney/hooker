package com.rbkmoney.hooker.dao;

import com.rbkmoney.damsel.webhooker.EventFilter;
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
        Set<WebhookAdditionalFilter> webhookAdditionalFilters = new HashSet<>();
        webhookAdditionalFilters.add(new WebhookAdditionalFilter(EventTypeCode.INVOICE_PAYMENT_STATUS_CHANGED, 34, null, "cancelled"));
        webhookAdditionalFilters.add(new WebhookAdditionalFilter(EventTypeCode.INVOICE_CREATED));
        EventFilter eventFilterByCode = EventFilterUtils.getEventFilterByCode(webhookAdditionalFilters);
        eventFilterByCode.getInvoice().setShopId(1);
        WebhookParams webhookParams = new WebhookParams("123", eventFilterByCode, "https://google.com");
        webhookDao.addWebhook(webhookParams);
        webhookAdditionalFilters.clear();
        webhookAdditionalFilters.add(new WebhookAdditionalFilter(EventTypeCode.INVOICE_STATUS_CHANGED, 78, "unpaid", null));
        webhookAdditionalFilters.add(new WebhookAdditionalFilter(EventTypeCode.INVOICE_PAYMENT_STARTED, 78));
        webhookParams = new WebhookParams("999", EventFilterUtils.getEventFilterByCode(webhookAdditionalFilters), "https://yandex.ru");
        webhookDao.addWebhook(webhookParams);
        webhookAdditionalFilters.clear();
        webhookAdditionalFilters.add(new WebhookAdditionalFilter(EventTypeCode.INVOICE_STATUS_CHANGED));
        webhookParams = new WebhookParams("123", EventFilterUtils.getEventFilterByCode(webhookAdditionalFilters), "https://2ch.hk/b");
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
        Assert.assertTrue(webhookDao.getWebhooksForInvoices(EventTypeCode.INVOICE_CREATED, "888", 44).isEmpty());
    }

    @Test
    public void getWebhooksForInvoices() throws Exception {
        Assert.assertFalse(webhookDao.getWebhooksForInvoices(EventTypeCode.INVOICE_PAYMENT_STARTED, "999", 78).isEmpty());
        Assert.assertTrue(webhookDao.getWebhooksForInvoices(EventTypeCode.INVOICE_PAYMENT_STARTED, "999", 79).isEmpty());
    }

    @Test
    public void getWebhooksForInvoiceStatusChanged() throws Exception {
        Assert.assertFalse(webhookDao.getWebhooksForInvoiceStatusChanged(EventTypeCode.INVOICE_STATUS_CHANGED, "999", 78, "unpaid").isEmpty());
        Assert.assertTrue(webhookDao.getWebhooksForInvoiceStatusChanged(EventTypeCode.INVOICE_STATUS_CHANGED, "999", 78, "cancelled").isEmpty());
    }

    @Test
    public void getWebhooksForInvoicePaymentStatusChanged() throws Exception {

    }

    @Test
    public void getPairKey() throws Exception {
        Assert.assertNotNull(webhookDao.getPairKey("123"));
        Assert.assertNull(webhookDao.getPairKey("88888"));
    }
}
