package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rbkmoney.hooker.handler.poller.impl.AbstractInvoiceEventHandler;
import com.rbkmoney.hooker.utils.BuildUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by inalarsanukaev on 24.04.17.
 */
public class MessageJsonTest {
    @Test
    public void test() throws JsonProcessingException {
        Message message = BuildUtils.message(AbstractInvoiceEventHandler.PAYMENT, "444", "987", EventType.INVOICE_PAYMENT_STARTED, "cancelled");
        System.out.println(MessageJson.buildMessageJson(message));
        Message copy = message.copy();
        message.getInvoice().setAmount(99988);
        Assert.assertNotEquals(message.getInvoice().getAmount(), copy.getInvoice().getAmount());
    }

    @Test
    public void testCart() throws JsonProcessingException {
        Message message = BuildUtils.message(AbstractInvoiceEventHandler.PAYMENT, "444", "987", EventType.INVOICE_PAYMENT_STARTED, "cancelled", BuildUtils.cart());
        String messageJson = MessageJson.buildMessageJson(message);
        System.out.println(messageJson);
        Assert.assertTrue(messageJson.contains("taxMode"));
    }
}
