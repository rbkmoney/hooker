package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.webhooker.EventFilter;
import com.rbkmoney.hooker.dao.WebhookAdditionalFilter;
import com.rbkmoney.hooker.model.EventType;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by inalarsanukaev on 10.04.17.
 */
public class EventFilterUtilsTest {
    @Test
    public void getEventFilterByCode() throws Exception {
        EventFilter eventFilterByCode = getEventFilter();
        Assert.assertEquals(eventFilterByCode.getInvoice().getTypes().size(), 4);
    }

    private EventFilter getEventFilter() {
        Collection<WebhookAdditionalFilter> eventTypeCodeSet = new HashSet<>();
        eventTypeCodeSet.add(new WebhookAdditionalFilter(EventType.INVOICE_CREATED));
        eventTypeCodeSet.add(new WebhookAdditionalFilter(EventType.INVOICE_PAYMENT_STARTED, 77));
        eventTypeCodeSet.add(new WebhookAdditionalFilter(EventType.INVOICE_PAYMENT_STATUS_CHANGED));
        eventTypeCodeSet.add(new WebhookAdditionalFilter(EventType.INVOICE_STATUS_CHANGED));
        return EventFilterUtils.getEventFilter(eventTypeCodeSet);
    }

    @Test
    public void getWebhookAdditionalFilter() throws Exception {
        Collection<WebhookAdditionalFilter> eventTypeCodeSet = EventFilterUtils.getWebhookAdditionalFilter(getEventFilter());
        Assert.assertEquals(eventTypeCodeSet.size(), 4);
    }

}