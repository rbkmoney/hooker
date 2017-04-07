package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.webhooker.*;
import com.rbkmoney.hooker.dao.EventTypeCode;

/**
 * Created by inalarsanukaev on 05.04.17.
 */
public class EventFilterUtils {
    public static EventFilter getEventFilterByCode(EventTypeCode eventTypeCode) {
        EventFilter eventFilter = new EventFilter();
        switch (eventTypeCode){
            case INVOICE_CREATED:
                eventFilter.setInvoice(new InvoiceEventFilter(InvoiceEventType.created(new InvoiceCreated())));
                break;
            case INVOICE_STATUS_CHANGED:
                eventFilter.setInvoice(new InvoiceEventFilter(InvoiceEventType.status_changed(new InvoiceStatusChanged())));
                break;
            case INVOICE_PAYMENT_STARTED:
                eventFilter.setInvoice(new InvoiceEventFilter(InvoiceEventType.payment(InvoicePaymentEventType.created(new InvoicePaymentCreated()))));
                break;
            case INVOICE_PAYMENT_STATUS_CHANGED:
                eventFilter.setInvoice(new InvoiceEventFilter(InvoiceEventType.payment(InvoicePaymentEventType.status_changed(new InvoicePaymentStatusChanged()))));
                break;
            default:
                return null;
        }
        return eventFilter;
    }

    public static EventTypeCode getEventTypeCodeByFilter(EventFilter eventFilter){
        if (eventFilter.isSetInvoice()) {
            if (eventFilter.getInvoice().getType().isSetCreated()) {
                return EventTypeCode.INVOICE_CREATED;
            }
            if (eventFilter.getInvoice().getType().isSetStatusChanged()) {
                return EventTypeCode.INVOICE_STATUS_CHANGED;
            }
            if (eventFilter.getInvoice().getType().isSetPayment()) {
                if (eventFilter.getInvoice().getType().getPayment().isSetCreated()) {
                    return EventTypeCode.INVOICE_PAYMENT_STARTED;
                }
                if (eventFilter.getInvoice().getType().getPayment().isSetStatusChanged()) {
                    return EventTypeCode.INVOICE_PAYMENT_STATUS_CHANGED;
                }
            }
        }
        return null;
    }
}
