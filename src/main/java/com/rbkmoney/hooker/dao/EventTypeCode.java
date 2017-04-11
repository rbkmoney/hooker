package com.rbkmoney.hooker.dao;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by inal on 30.11.2016.
 */
public enum EventTypeCode {
    INVOICE_CREATED               ("source_event.processing_event.payload.invoice_event.invoice_created.invoice"),
    INVOICE_STATUS_CHANGED        ("source_event.processing_event.payload.invoice_event.invoice_status_changed.status"),
    INVOICE_PAYMENT_STARTED       ("source_event.processing_event.payload.invoice_event.invoice_payment_event.invoice_payment_started.payment"),
    INVOICE_PAYMENT_STATUS_CHANGED("source_event.processing_event.payload.invoice_event.invoice_payment_event.invoice_payment_status_changed.status");

    private static class Holder {
        static Map<String, EventTypeCode> MAP = new HashMap<>();
    }

    private String key;

    public static EventTypeCode valueOfKey(String code) {
        return Holder.MAP.get(code);
    }

    EventTypeCode(String key) {
        this.key = key;
        Holder.MAP.put(key, this);
    }

    public String getKey() {
        return key;
    }
}
