package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.webhooker.EventFilter;
import com.rbkmoney.hooker.model.EventType;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

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
        HashSet<EventType> eventTypeSet = new HashSet<>();
        eventTypeSet.add(EventType.INVOICE_CREATED);
        eventTypeSet.add(EventType.INVOICE_PAYMENT_STARTED);
        eventTypeSet.add(EventType.INVOICE_PAYMENT_STATUS_CHANGED);
        eventTypeSet.add(EventType.INVOICE_STATUS_CHANGED);
        return EventFilterUtils.getEventFilter(eventTypeSet);
    }

    @Test
    public void getEventTypeCodeSetByFilter() throws Exception {
        Set<EventType> eventTypeSet = EventFilterUtils.getEventTypes(getEventFilter());
        Assert.assertEquals(eventTypeSet.size(), 4);
    }

}
