package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.swag_webhook_events.model.Event;

public interface EventService<M extends Message> {

    Event getEventByMessage(M message);

    default InvoicePayment getPaymentByMessage(M message) {
        throw new RuntimeException("Method not implemented");
    }

}
