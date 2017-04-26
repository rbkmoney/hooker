package com.rbkmoney.hooker.utils;

import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Message;

/**
 * Created by jeckep on 25.04.17.
 */
public class BuildUtils {
    public static Message message(String invoceId, String partyId, EventType type, String status) {
        Message message = new Message();
        message.setEventId(5555);
        message.setInvoiceId(invoceId);
        message.setPartyId(partyId);
        message.setShopId(123);
        message.setAmount(12235);
        message.setCurrency("RUB");
        message.setCreatedAt("12.12.2008");
        com.rbkmoney.damsel.base.Content metadata = new com.rbkmoney.damsel.base.Content();
        metadata.setType("string");
        metadata.setData("somedata".getBytes());
        message.setMetadata(metadata);
        message.setProduct("product");
        message.setDescription("description");
        message.setEventType(type);
        message.setType("invoice");
        message.setStatus(status);
        message.setPaymentId("paymentId");
        message.setEventTime("time");
        return message;
    }
}
