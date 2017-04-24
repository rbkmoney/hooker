package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rbkmoney.damsel.base.Content;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by inalarsanukaev on 24.04.17.
 */
public class MessageJsonTest {
    @Test
    public void test() throws JsonProcessingException {
        Message message = new Message();
        message.setEventId(5555);
        message.setInvoiceId("ffsfgsr");
        message.setPartyId("sdrgsr");
        message.setShopId(123);
        message.setAmount(12235);
        message.setCurrency("RUB");
        message.setCreatedAt("12.12.2008");
        message.setType("invoice");
        message.setProduct("product");
        message.setDescription("description");
        message.setEventType(EventType.INVOICE_CREATED);
        message.setStatus("paid");
        Content metadata = new Content();
        metadata.setType("string");
        metadata.setData("somedata".getBytes());
        message.setMetadata(metadata);
        System.out.print(MessageJson.buildMessageJson(message));
    }
}
