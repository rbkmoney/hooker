package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.webhooker.*;
import com.rbkmoney.hooker.dao.EventTypeCode;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by inalarsanukaev on 05.04.17.
 */
public class EventFilterUtils {
    public static EventFilter getEventFilterByCode(Collection<EventTypeCode> eventTypeCodeSet) {
        if (eventTypeCodeSet == null || eventTypeCodeSet.isEmpty()) {return null;}
        EventFilter eventFilter = new EventFilter();
        InvoiceEventFilter invoiceEventFilter = new InvoiceEventFilter();
        Set<InvoiceEventType> invoiceEventTypes = new HashSet<>();
        invoiceEventFilter.setTypes(invoiceEventTypes);
        eventFilter.setInvoice(invoiceEventFilter);
        for (EventTypeCode eventTypeCode : eventTypeCodeSet) {
            switch (eventTypeCode) {
                case INVOICE_CREATED:
                    invoiceEventTypes.add(InvoiceEventType.created(new InvoiceCreated()));
                    break;
                case INVOICE_STATUS_CHANGED:
                    invoiceEventTypes.add(InvoiceEventType.status_changed(new InvoiceStatusChanged()));
                    break;
                case INVOICE_PAYMENT_STARTED:
                    invoiceEventTypes.add(InvoiceEventType.payment(InvoicePaymentEventType.created(new InvoicePaymentCreated())));
                    break;
                case INVOICE_PAYMENT_STATUS_CHANGED:
                    invoiceEventTypes.add(InvoiceEventType.payment(InvoicePaymentEventType.status_changed(new InvoicePaymentStatusChanged())));
                    break;
                default:
                    return null;
            }
        }
        return eventFilter;
    }

    public static Set<EventTypeCode> getEventTypeCodeSetByFilter(EventFilter eventFilter){
        Set<EventTypeCode> eventTypeCodeSet = new HashSet<>();
        if (eventFilter.isSetInvoice()) {
            Set<InvoiceEventType> invoiceEventTypes = eventFilter.getInvoice().getTypes();
            for (InvoiceEventType invoiceEventType : invoiceEventTypes) {

                if (invoiceEventType.isSetCreated()) {
                    eventTypeCodeSet.add(EventTypeCode.INVOICE_CREATED);
                }
                if (invoiceEventType.isSetStatusChanged()) {
                    eventTypeCodeSet.add(EventTypeCode.INVOICE_STATUS_CHANGED);
                }
                if (invoiceEventType.isSetPayment()) {
                    if (invoiceEventType.getPayment().isSetCreated()) {
                        eventTypeCodeSet.add(EventTypeCode.INVOICE_PAYMENT_STARTED);
                    }
                    if (invoiceEventType.getPayment().isSetStatusChanged()) {
                        eventTypeCodeSet.add(EventTypeCode.INVOICE_PAYMENT_STATUS_CHANGED);
                    }
                }
            }
        }
        return eventTypeCodeSet;
    }

    public static List<String> getCodes(Collection<EventTypeCode> coll){
        return coll.stream().map(etc -> etc.getKey()).collect(Collectors.toList());
    }

}
