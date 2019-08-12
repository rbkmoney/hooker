package com.rbkmoney.hooker.dao;

import com.rbkmoney.damsel.webhooker.EventFilter;
import com.rbkmoney.damsel.webhooker.WebhookParams;
import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.utils.EventFilterUtils;
import com.rbkmoney.hooker.utils.HookConverter;
import com.rbkmoney.swag_webhook_events.model.Event;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Created by inalarsanukaev on 08.04.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HookDaoImplTest extends AbstractIntegrationTest {

    @Autowired
    HookDao hookDao;

    List<Long> ids = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        Set<WebhookAdditionalFilter> webhookAdditionalFilters = new HashSet<>();
        webhookAdditionalFilters.add(new WebhookAdditionalFilter(EventType.INVOICE_PAYMENT_STATUS_CHANGED, "34", null, "cancelled", null));
        webhookAdditionalFilters.add(WebhookAdditionalFilter.builder().eventType(EventType.INVOICE_CREATED).build());
        EventFilter eventFilterByCode = EventFilterUtils.getEventFilter(webhookAdditionalFilters);
        eventFilterByCode.getInvoice().setShopId("1");
        WebhookParams webhookParams = new WebhookParams("123", eventFilterByCode, "https://google.com");
        Hook hook = hookDao.create(HookConverter.convert(webhookParams));
        String pubKey1 = hook.getPubKey();
        ids.add(hook.getId());
        webhookAdditionalFilters.clear();
        webhookAdditionalFilters.add(new WebhookAdditionalFilter(EventType.INVOICE_STATUS_CHANGED, "78", "unpaid", null, null));
        webhookAdditionalFilters.add(WebhookAdditionalFilter.builder().eventType(EventType.INVOICE_PAYMENT_STARTED).shopId("78").build());
        webhookParams = new WebhookParams("999", EventFilterUtils.getEventFilter(webhookAdditionalFilters), "https://yandex.ru");
        hook = hookDao.create(HookConverter.convert(webhookParams));
        ids.add(hook.getId());
        webhookAdditionalFilters.clear();
        webhookAdditionalFilters.add(WebhookAdditionalFilter.builder().eventType(EventType.INVOICE_STATUS_CHANGED).build());
        webhookParams = new WebhookParams("123", EventFilterUtils.getEventFilter(webhookAdditionalFilters), "https://2ch.hk/b");
        hook = hookDao.create(HookConverter.convert(webhookParams));
        String pubKey2 = hook.getPubKey();
        ids.add(hook.getId());
        Assert.assertEquals(pubKey1, pubKey2);
        webhookAdditionalFilters.clear();
        webhookAdditionalFilters.add(WebhookAdditionalFilter.builder().eventType(EventType.CUSTOMER_CREATED).build());
        webhookAdditionalFilters.add(WebhookAdditionalFilter.builder().eventType(EventType.CUSTOMER_BINDING_SUCCEEDED).build());
        webhookParams = new WebhookParams("123", EventFilterUtils.getEventFilter(webhookAdditionalFilters), "https://2ch.hk/b");
        hook = hookDao.create(HookConverter.convert(webhookParams));
    }

    @After
    public void tearDown() throws Exception {
        List<Hook> list = hookDao.getPartyHooks("123");
        for (Hook w : list) {
            hookDao.delete(w.getId());
        }
        list = hookDao.getPartyHooks("999");
        for (Hook w : list) {
            hookDao.delete(w.getId());
        }
    }

    @Test
    public void testConstraint(){
        Set<WebhookAdditionalFilter> webhookAdditionalFilters = new HashSet<>();
        webhookAdditionalFilters.add(new WebhookAdditionalFilter(EventType.INVOICE_PAYMENT_STATUS_CHANGED, "34", null, "failed", "succeeded"));
        webhookAdditionalFilters.add(new WebhookAdditionalFilter(EventType.INVOICE_PAYMENT_STATUS_CHANGED, "34", null, "pending", null));
        WebhookParams webhookParams  = new WebhookParams("123", EventFilterUtils.getEventFilter(webhookAdditionalFilters), "https://2ch.hk/b");
        Hook hook = hookDao.create(HookConverter.convert(webhookParams));
        ids.add(hook.getId());
    }

    @Test
    public void getPartyWebhooks() throws Exception {
        assertEquals(hookDao.getPartyHooks("123").stream().filter(Hook::isEnabled).collect(Collectors.toList()).size(), 3);
        Assert.assertTrue(hookDao.getPartyHooks("88888").stream().filter(Hook::isEnabled).collect(Collectors.toList()).isEmpty());
    }

    @Test
    public void getWebhookById() throws Exception {
        List<Hook> list = hookDao.getPartyHooks("123");
        for (Hook w : list) {
            Assert.assertNotNull(hookDao.getHookById(w.getId()));
        }
    }

    public static Hook buildHook(String partyId, String url){
        Hook hook = new Hook();
        hook.setPartyId(partyId);
        hook.setUrl(url);
        hook.setTopic(Event.TopicEnum.INVOICESTOPIC.getValue());

        Set<WebhookAdditionalFilter> webhookAdditionalFilters = new HashSet<>();
        webhookAdditionalFilters.add(new WebhookAdditionalFilter(EventType.INVOICE_PAYMENT_STATUS_CHANGED, "34", null, "cancelled", "failed"));
        webhookAdditionalFilters.add(WebhookAdditionalFilter.builder().eventType(EventType.INVOICE_CREATED).build());
        hook.setFilters(webhookAdditionalFilters);

        return hook;
    }
}
