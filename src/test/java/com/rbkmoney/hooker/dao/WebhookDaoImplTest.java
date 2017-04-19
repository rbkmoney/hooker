package com.rbkmoney.hooker.dao;

import com.rbkmoney.damsel.webhooker.EventFilter;
import com.rbkmoney.damsel.webhooker.WebhookParams;
import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.utils.EventFilterUtils;
import com.rbkmoney.hooker.utils.HookConverter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by inalarsanukaev on 08.04.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebhookDaoImplTest extends AbstractIntegrationTest {

    @Autowired
    WebhookDao webhookDao;

    List<Long> ids = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        Set<WebhookAdditionalFilter> webhookAdditionalFilters = new HashSet<>();
        webhookAdditionalFilters.add(new WebhookAdditionalFilter(EventType.INVOICE_PAYMENT_STATUS_CHANGED, 34, null, "cancelled"));
        webhookAdditionalFilters.add(new WebhookAdditionalFilter(EventType.INVOICE_CREATED));
        EventFilter eventFilterByCode = EventFilterUtils.getEventFilter(webhookAdditionalFilters);
        eventFilterByCode.getInvoice().setShopId(1);
        WebhookParams webhookParams = new WebhookParams("123", eventFilterByCode, "https://google.com");
        Hook hook = webhookDao.create(HookConverter.convert(webhookParams));
        ids.add(hook.getId());
        webhookAdditionalFilters.clear();
        webhookAdditionalFilters.add(new WebhookAdditionalFilter(EventType.INVOICE_STATUS_CHANGED, 78, "unpaid", null));
        webhookAdditionalFilters.add(new WebhookAdditionalFilter(EventType.INVOICE_PAYMENT_STARTED, 78));
        webhookParams = new WebhookParams("999", EventFilterUtils.getEventFilter(webhookAdditionalFilters), "https://yandex.ru");
        hook = webhookDao.create(HookConverter.convert(webhookParams));
        ids.add(hook.getId());
        webhookAdditionalFilters.clear();
        webhookAdditionalFilters.add(new WebhookAdditionalFilter(EventType.INVOICE_STATUS_CHANGED));
        webhookParams = new WebhookParams("123", EventFilterUtils.getEventFilter(webhookAdditionalFilters), "https://2ch.hk/b");
        hook = webhookDao.create(HookConverter.convert(webhookParams));
        ids.add(hook.getId());
    }

    @After
    public void tearDown() throws Exception {
        List<Hook> list = webhookDao.getPartyWebhooks("123");
        for (Hook w : list) {
            webhookDao.delete(w.getId());
        }
        list = webhookDao.getPartyWebhooks("999");
        for (Hook w : list) {
            webhookDao.delete(w.getId());
        }
    }

    @Test
    public void getPartyWebhooks() throws Exception {
        assertEquals(webhookDao.getPartyWebhooks("123").size(), 2);
        Assert.assertTrue(webhookDao.getPartyWebhooks("88888").isEmpty());
    }

    @Test
    public void getWebhookById() throws Exception {
        List<Hook> list = webhookDao.getPartyWebhooks("123");
        for (Hook w : list) {
            System.out.println(w);
            Assert.assertNotNull(webhookDao.getWebhookById(w.getId()));
        }
    }

    @Test
    public void getByIds(){
        List<Hook> hooks = webhookDao.getWithPolicies(ids);
        assertEquals(3, hooks.size());
    }

    public static Hook buildHook(String partyId, String url){
        Hook hook = new Hook();
        hook.setPartyId(partyId);
        hook.setUrl(url);

        Set<WebhookAdditionalFilter> webhookAdditionalFilters = new HashSet<>();
        webhookAdditionalFilters.add(new WebhookAdditionalFilter(EventType.INVOICE_PAYMENT_STATUS_CHANGED, 34, null, "cancelled"));
        webhookAdditionalFilters.add(new WebhookAdditionalFilter(EventType.INVOICE_CREATED));
        hook.setFilters(webhookAdditionalFilters);

        return hook;
    }
}
