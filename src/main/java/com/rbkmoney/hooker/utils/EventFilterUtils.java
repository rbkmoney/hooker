package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.webhooker.*;
import com.rbkmoney.hooker.model.EventType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by inalarsanukaev on 05.04.17.
 */
public class EventFilterUtils {
    public static EventFilter getEventFilter(Collection<EventType> eventTypeSet) {
        if (eventTypeSet == null || eventTypeSet.isEmpty()) {return null;}
        EventFilter eventFilter = new EventFilter();
        InvoiceEventFilter invoiceEventFilter = new InvoiceEventFilter();
        Set<InvoiceEventType> invoiceEventTypes = new HashSet<>();
        invoiceEventFilter.setTypes(invoiceEventTypes);
        eventFilter.setInvoice(invoiceEventFilter);
        for (EventType eventType : eventTypeSet) {
            switch (eventType) {
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

    public static Set<EventType> getEventTypes(EventFilter eventFilter){
        Set<EventType> eventTypeSet = new HashSet<>();
        if (eventFilter.isSetInvoice()) {
            Set<InvoiceEventType> invoiceEventTypes = eventFilter.getInvoice().getTypes();
            for (InvoiceEventType invoiceEventType : invoiceEventTypes) {

                if (invoiceEventType.isSetCreated()) {
                    eventTypeSet.add(EventType.INVOICE_CREATED);
                }
                if (invoiceEventType.isSetStatusChanged()) {
                    eventTypeSet.add(EventType.INVOICE_STATUS_CHANGED);
                }
                if (invoiceEventType.isSetPayment()) {
                    if (invoiceEventType.getPayment().isSetCreated()) {
                        eventTypeSet.add(EventType.INVOICE_PAYMENT_STARTED);
                    }
                    if (invoiceEventType.getPayment().isSetStatusChanged()) {
                        eventTypeSet.add(EventType.INVOICE_PAYMENT_STATUS_CHANGED);
                    }
                }
            }
        }
        return eventTypeSet;
    }
}
