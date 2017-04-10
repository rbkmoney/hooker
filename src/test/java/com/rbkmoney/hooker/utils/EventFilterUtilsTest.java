package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.webhooker.EventFilter;
import com.rbkmoney.hooker.dao.EventTypeCode;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

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
        HashSet<EventTypeCode> eventTypeCodeSet = new HashSet<>();
        eventTypeCodeSet.add(EventTypeCode.INVOICE_CREATED);
        eventTypeCodeSet.add(EventTypeCode.INVOICE_PAYMENT_STARTED);
        eventTypeCodeSet.add(EventTypeCode.INVOICE_PAYMENT_STATUS_CHANGED);
        eventTypeCodeSet.add(EventTypeCode.INVOICE_STATUS_CHANGED);
        return EventFilterUtils.getEventFilterByCode(eventTypeCodeSet);
    }

    @Test
    public void getEventTypeCodeSetByFilter() throws Exception {
        Set<EventTypeCode> eventTypeCodeSet = EventFilterUtils.getEventTypeCodeSetByFilter(getEventFilter());
        Assert.assertEquals(eventTypeCodeSet.size(), 4);
    }

}
